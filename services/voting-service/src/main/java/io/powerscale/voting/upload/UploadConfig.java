package io.powerscale.voting.upload;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadConfig implements WebMvcConfigurer {

    private final ImageStorageService imageStorageService;

    public UploadConfig(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = imageStorageService.directory().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/api/uploads/**").addResourceLocations(location);
    }
}
