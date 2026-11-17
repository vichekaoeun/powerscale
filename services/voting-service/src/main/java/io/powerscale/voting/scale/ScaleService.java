package io.powerscale.voting.scale;

import io.powerscale.events.RankingFormula;
import io.powerscale.voting.character.PageResponse;
import io.powerscale.voting.outbox.OutboxService;
import io.powerscale.voting.vote.Difficulty;
import io.powerscale.voting.web.BadRequestException;
import io.powerscale.voting.web.ConflictException;
import io.powerscale.voting.web.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ScaleService {

    private static final int MAX_USER_ID_LENGTH = 128;
    private static final int MAX_ITEMS = 200;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_WRITE_ATTEMPTS = 3;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ScaleRepository scaleRepository;
    private final ScaleItemRepository itemRepository;
    private final ScaleMatchupRepository matchupRepository;
    private final ScaleVoteRepository voteRepository;
    private final OutboxService outboxService;
    private final ScaleVoteEventFactory eventFactory;
    private final TransactionTemplate transactionTemplate;

    public ScaleService(
            ScaleRepository scaleRepository,
            ScaleItemRepository itemRepository,
            ScaleMatchupRepository matchupRepository,
            ScaleVoteRepository voteRepository,
            OutboxService outboxService,
            ScaleVoteEventFactory eventFactory,
            TransactionTemplate transactionTemplate
    ) {
        this.scaleRepository = scaleRepository;
        this.itemRepository = itemRepository;
        this.matchupRepository = matchupRepository;
        this.voteRepository = voteRepository;
        this.outboxService = outboxService;
        this.eventFactory = eventFactory;
        this.transactionTemplate = transactionTemplate;
    }

    @Transactional
    public CreateScaleResponse createScale(CreateScaleRequest request) {
        List<ScaleItemRequest> items = normalizeItems(request.items());
        if (items.size() < 2) {
            throw new BadRequestException("A scale needs at least two unique items");
        }

        String creatorKey = generateCreatorKey();
        Scale scale = scaleRepository.save(new Scale(
                generateSlug(request.title()),
                request.title().trim(),
                request.criterion().trim(),
                normalizeDescription(request.description()),
                hash(creatorKey),
                normalizeFormula(request.formula()),
                normalizeSelections(request.selections())
        ));

        List<ScaleItem> savedItems = itemRepository.saveAll(items.stream()
                .map(item -> new ScaleItem(scale.getId(), item.name(), item.imageUrl()))
                .toList());

        return new CreateScaleResponse(ScaleResponse.from(scale, savedItems), creatorKey);
    }

    @Transactional(readOnly = true)
    public PageResponse<ScaleSummaryResponse> listScales(String search, int page, int size) {
        if (page < 0) {
            throw new BadRequestException("page must be zero or greater");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("size must be between 1 and " + MAX_PAGE_SIZE);
        }

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("title"))
        );
        Page<Scale> scales = (search == null || search.isBlank())
                ? scaleRepository.findAll(pageRequest)
                : scaleRepository.search(search.trim(), pageRequest);

        return PageResponse.from(scales, scale -> new ScaleSummaryResponse(
                scale.getId(),
                scale.getSlug(),
                scale.getTitle(),
                scale.getCriterion(),
                scale.getFormula().method(),
                scale.getDescription(),
                (int) itemRepository.countByScaleId(scale.getId()),
                voteRepository.countByScaleId(scale.getId()),
                scale.getCreatedAt()
        ));
    }

    @Transactional(readOnly = true)
    public ScaleResponse getScale(String slug) {
        Scale scale = findScale(slug);
        return ScaleResponse.from(scale, itemRepository.findByScaleIdOrderByNameAsc(scale.getId()));
    }

    @Transactional
    public ScaleResponse addItems(String slug, AddScaleItemsRequest request) {
        Scale scale = findScale(slug);

        List<ScaleItemRequest> items = normalizeItems(request.items());
        List<ScaleItem> toSave = new ArrayList<>();
        Set<String> existingNames = new HashSet<>();
        for (ScaleItem item : itemRepository.findByScaleIdOrderByNameAsc(scale.getId())) {
            existingNames.add(item.getName().toLowerCase(Locale.ROOT));
        }

        int currentCount = existingNames.size();
        for (ScaleItemRequest item : items) {
            String normalizedName = item.name().toLowerCase(Locale.ROOT);
            if (!existingNames.add(normalizedName)) {
                continue;
            }
            toSave.add(new ScaleItem(scale.getId(), item.name(), item.imageUrl()));
            if (currentCount + toSave.size() > MAX_ITEMS) {
                throw new BadRequestException("A scale can have at most " + MAX_ITEMS + " items");
            }
        }

        itemRepository.saveAll(toSave);
        return ScaleResponse.from(scale, itemRepository.findByScaleIdOrderByNameAsc(scale.getId()));
    }

    @Transactional
    public ScaleMatchupResponse getOrCreateRandomMatchup(String slug) {
        Scale scale = findScale(slug);
        List<UUID> itemIds = itemRepository.findAllIdsByScaleId(scale.getId());
        if (itemIds.size() < 2) {
            throw new ConflictException("At least two items are required before matchups can be generated");
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int firstIndex = random.nextInt(itemIds.size());
        int secondIndex = random.nextInt(itemIds.size() - 1);
        if (secondIndex >= firstIndex) {
            secondIndex++;
        }

        ScaleItem first = itemRepository.findById(itemIds.get(firstIndex))
                .orElseThrow(() -> new ResourceNotFoundException("Scale item not found"));
        ScaleItem second = itemRepository.findById(itemIds.get(secondIndex))
                .orElseThrow(() -> new ResourceNotFoundException("Scale item not found"));

        ScaleItem itemA = first;
        ScaleItem itemB = second;
        if (itemA.getId().compareTo(itemB.getId()) > 0) {
            itemA = second;
            itemB = first;
        }

        ScaleItem finalItemA = itemA;
        ScaleItem finalItemB = itemB;
        ScaleMatchup matchup = matchupRepository
                .findByScaleIdAndItemAIdAndItemBId(scale.getId(), finalItemA.getId(), finalItemB.getId())
                .orElseGet(() -> matchupRepository.save(
                        new ScaleMatchup(scale.getId(), finalItemA, finalItemB)
                ));

        return ScaleMatchupResponse.from(matchup);
    }

    @Transactional(readOnly = true)
    public ScaleMatchupStatsResponse stats(String slug, UUID matchupId) {
        Scale scale = findScale(slug);
        ScaleMatchup matchup = findMatchup(matchupId);
        if (!matchup.getScaleId().equals(scale.getId())) {
            throw new ResourceNotFoundException("Matchup does not belong to this scale");
        }

        List<ScaleVoteRepository.OutcomeCount> counts = voteRepository.countOutcomes(matchupId);
        Map<OutcomeKey, Long> votesByOutcome = new HashMap<>();
        for (ScaleVoteRepository.OutcomeCount count : counts) {
            votesByOutcome.put(new OutcomeKey(count.getWinnerId(), count.getDifficulty()), count.getVotes());
        }

        long totalVotes = votesByOutcome.values().stream().mapToLong(Long::longValue).sum();
        List<ScaleOutcomeStat> outcomes = new ArrayList<>();
        int selectionCount = scale.getSelections().size();
        for (ScaleItem item : List.of(matchup.getItemA(), matchup.getItemB())) {
            for (int slot = 0; slot < selectionCount; slot++) {
                Difficulty difficulty = Difficulty.values()[slot];
                long votes = votesByOutcome.getOrDefault(new OutcomeKey(item.getId(), difficulty), 0L);
                double percentage = totalVotes == 0
                        ? 0.0
                        : Math.round(votes * 10_000.0 / totalVotes) / 100.0;
                outcomes.add(new ScaleOutcomeStat(item.getId(), difficulty, votes, percentage));
            }
        }

        return new ScaleMatchupStatsResponse(
                matchup.getId(),
                ScaleItemResponse.from(matchup.getItemA()),
                ScaleItemResponse.from(matchup.getItemB()),
                totalVotes,
                outcomes
        );
    }

    public ScaleVoteResponse submitVote(String slug, String userId, ScaleVoteRequest request) {
        String normalizedUserId = normalizeUserId(userId);

        for (int attempt = 1; ; attempt++) {
            try {
                ScaleVoteResponse response = transactionTemplate.execute(
                        status -> doSubmitVote(slug, normalizedUserId, request)
                );
                if (response == null) {
                    throw new IllegalStateException("Scale vote transaction returned no result");
                }
                return response;
            } catch (DataIntegrityViolationException | ObjectOptimisticLockingFailureException exception) {
                if (attempt >= MAX_WRITE_ATTEMPTS) {
                    throw exception;
                }
                backoff(attempt);
            }
        }
    }

    @Transactional(readOnly = true)
    public Optional<ScaleVoteResponse> getUserVote(String slug, String userId, UUID matchupId) {
        Scale scale = findScale(slug);
        String normalizedUserId = normalizeUserId(userId);
        return voteRepository.findUserVoteWithDetails(normalizedUserId, matchupId)
                .filter(vote -> vote.getScaleId().equals(scale.getId()))
                .map(ScaleVoteResponse::from);
    }

    private ScaleVoteResponse doSubmitVote(String slug, String userId, ScaleVoteRequest request) {
        Scale scale = findScale(slug);
        ScaleMatchup matchup = findMatchup(request.matchupId());
        if (!matchup.getScaleId().equals(scale.getId())) {
            throw new BadRequestException("matchupId must belong to this scale");
        }
        if (request.difficulty().ordinal() >= scale.getSelections().size()) {
            throw new BadRequestException("selection is not available on this scale");
        }
        if (!matchup.includes(request.winnerId())) {
            throw new BadRequestException("winnerId must be one of the matchup's items");
        }

        ScaleItem winner = matchup.getItemA().getId().equals(request.winnerId())
                ? matchup.getItemA()
                : matchup.getItemB();

        ScaleVote vote = voteRepository.findByUserIdAndMatchupId(userId, matchup.getId()).orElse(null);
        if (vote == null) {
            vote = new ScaleVote(scale.getId(), matchup, userId, winner, request.difficulty());
        } else {
            vote.update(winner, request.difficulty());
        }

        ScaleVote saved = voteRepository.saveAndFlush(vote);
        outboxService.enqueueScaleVoteSubmitted(
                eventFactory.create(UUID.randomUUID(), saved, scale.getFormula(), Instant.now())
        );

        return ScaleVoteResponse.from(saved);
    }

    private Scale findScale(String slug) {
        return scaleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Scale %s not found".formatted(slug)));
    }

    private ScaleMatchup findMatchup(UUID id) {
        return matchupRepository.findWithItemsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matchup %s not found".formatted(id)));
    }

    private List<ScaleItemRequest> normalizeItems(List<ScaleItemRequest> items) {
        Map<String, ScaleItemRequest> unique = new HashMap<>();
        for (ScaleItemRequest item : items) {
            if (item == null || item.name() == null || item.name().isBlank()) {
                continue;
            }
            String name = item.name().trim();
            if (name.length() > 160) {
                throw new BadRequestException("Item names must be at most 160 characters");
            }
            String key = name.toLowerCase(Locale.ROOT);
            unique.putIfAbsent(key, new ScaleItemRequest(name, normalizeImageUrl(item.imageUrl())));
        }
        return new ArrayList<>(unique.values());
    }

    private String normalizeImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        String trimmed = imageUrl.trim();
        if (trimmed.startsWith("http://")
                || trimmed.startsWith("https://")
                || trimmed.startsWith("/api/uploads/")) {
            return trimmed;
        }
        return null;
    }

    private List<String> normalizeSelections(List<String> selections) {
        List<String> source = (selections == null || selections.isEmpty())
                ? List.of("Winner")
                : selections;
        if (source.size() > 3) {
            throw new BadRequestException("A scale can have at most 3 selections");
        }

        List<String> normalized = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String selection : source) {
            if (selection == null || selection.isBlank()) {
                throw new BadRequestException("Selection labels must not be blank");
            }
            String label = selection.trim();
            if (label.length() > 40) {
                throw new BadRequestException("Selection labels must be at most 40 characters");
            }
            String key = label.toLowerCase(Locale.ROOT);
            if (!seen.add(key)) {
                throw new BadRequestException("Selection labels must be unique");
            }
            normalized.add(label);
        }
        return normalized;
    }

    private RankingFormula normalizeFormula(RankingFormula formula) {
        RankingFormula candidate = formula == null ? RankingFormula.eloDefault() : formula;
        String method = candidate.method() == null || candidate.method().isBlank()
                ? "ELO"
                : candidate.method().trim().toUpperCase(Locale.ROOT);

        if (!method.equals("ELO") && !method.equals("POINTS")) {
            throw new BadRequestException("ranking method must be ELO or POINTS");
        }
        if (candidate.baseRating() < 100 || candidate.baseRating() > 5000) {
            throw new BadRequestException("base rating must be between 100 and 5000");
        }
        if (candidate.kFactor() < 1 || candidate.kFactor() > 200) {
            throw new BadRequestException("K factor must be between 1 and 200");
        }
        validateRange("low diff weight", candidate.lowWeight(), 0, 5);
        validateRange("mid diff weight", candidate.midWeight(), 0, 5);
        validateRange("high diff weight", candidate.highWeight(), 0, 5);
        validateRange("low diff points", candidate.lowPoints(), 0, 1000);
        validateRange("mid diff points", candidate.midPoints(), 0, 1000);
        validateRange("high diff points", candidate.highPoints(), 0, 1000);

        return new RankingFormula(
                method,
                candidate.baseRating(),
                candidate.kFactor(),
                candidate.lowWeight(),
                candidate.midWeight(),
                candidate.highWeight(),
                candidate.lowPoints(),
                candidate.midPoints(),
                candidate.highPoints()
        );
    }

    private void validateRange(String name, double value, double min, double max) {
        if (value < min || value > max) {
            throw new BadRequestException(name + " must be between " + min + " and " + max);
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    private String generateSlug(String title) {
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (normalized.isBlank()) {
            normalized = "scale";
        }
        if (normalized.length() > 60) {
            normalized = normalized.substring(0, 60).replaceAll("-$", "");
        }

        String slug;
        do {
            slug = normalized + "-" + randomToken(4);
        } while (scaleRepository.existsBySlug(slug));
        return slug;
    }

    private String generateCreatorKey() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String randomToken(int bytes) {
        byte[] random = new byte[bytes];
        SECURE_RANDOM.nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random)
                .replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase(Locale.ROOT);
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(25L * attempt);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while retrying scale vote", interrupted);
        }
    }

    private String normalizeUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("X-User-Id header must not be blank");
        }
        String normalized = userId.trim();
        if (normalized.length() > MAX_USER_ID_LENGTH) {
            throw new BadRequestException("X-User-Id header must be at most " + MAX_USER_ID_LENGTH + " characters");
        }
        return normalized;
    }

    private record OutcomeKey(UUID itemId, Difficulty difficulty) {
    }
}
