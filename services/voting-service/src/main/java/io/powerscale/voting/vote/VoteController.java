package io.powerscale.voting.vote;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping
    public VoteResponse submit(
            @RequestHeader(USER_ID_HEADER) String userId,
            @Valid @RequestBody SubmitVoteRequest request
    ) {
        return voteService.submit(userId, request);
    }

    @GetMapping("/me")
    public List<VoteResponse> myVotes(@RequestHeader(USER_ID_HEADER) String userId) {
        return voteService.getUserVotes(userId);
    }

    @GetMapping("/me/{matchupId}")
    public ResponseEntity<VoteResponse> myVoteForMatchup(
            @RequestHeader(USER_ID_HEADER) String userId,
            @PathVariable UUID matchupId
    ) {
        return voteService.getUserVote(userId, matchupId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
