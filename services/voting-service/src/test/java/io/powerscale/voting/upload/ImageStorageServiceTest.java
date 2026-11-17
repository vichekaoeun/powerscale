package io.powerscale.voting.upload;

import io.powerscale.voting.web.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storesSupportedImages() throws Exception {
        ImageStorageService storage = new ImageStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47}
        );

        String url = storage.store(file);

        assertTrue(url.startsWith("/api/uploads/"));
        assertTrue(Files.exists(tempDir.resolve(url.substring("/api/uploads/".length()))));
    }

    @Test
    void rejectsNonImages() {
        ImageStorageService storage = new ImageStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "hello".getBytes()
        );

        assertThrows(BadRequestException.class, () -> storage.store(file));
    }
}
