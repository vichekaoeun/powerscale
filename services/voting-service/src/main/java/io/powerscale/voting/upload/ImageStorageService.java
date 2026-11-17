package io.powerscale.voting.upload;

import io.powerscale.voting.web.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif"
    );

    private final Path directory;

    public ImageStorageService(@Value("${app.uploads.dir:./uploads}") String uploadDirectory) {
        this.directory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(directory);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create upload directory", exception);
        }
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new BadRequestException("Image must be 5 MB or smaller");
        }

        String extension = EXTENSIONS.get(file.getContentType());
        if (extension == null) {
            throw new BadRequestException("Only JPEG, PNG, WebP, and GIF images are supported");
        }

        String filename = UUID.randomUUID() + "." + extension;
        Path target = directory.resolve(filename).normalize();
        if (!target.startsWith(directory)) {
            throw new BadRequestException("Invalid image path");
        }

        try {
            Files.copy(file.getInputStream(), target);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save image", exception);
        }

        return "/api/uploads/" + filename;
    }

    public Path directory() {
        return directory;
    }
}
