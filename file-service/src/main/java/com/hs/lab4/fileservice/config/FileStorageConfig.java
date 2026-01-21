package com.hs.lab4.fileservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ConfigurationProperties(prefix = "file.storage")
@Getter
@Setter
public class FileStorageConfig {
    private String uploadDir = "uploads";

    public Path getUploadPath() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }
}
