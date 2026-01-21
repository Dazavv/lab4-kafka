package com.hs.lab4.fileservice.controller;

import com.hs.lab4.userservice.FileServiceApplication;
import com.hs.lab4.userservice.controller.FileController;
import com.hs.lab4.userservice.dto.FileDto;
import com.hs.lab4.userservice.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.when;

@WebFluxTest(controllers = FileController.class)
@Import(TestSecurityConfig.class)
class FileControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private FileService fileService;

//    @Mock
//    private FileStorageConfig fileStorageConfig;

    @TempDir
    Path tempDir;

    private FileDto testDto;
//    private FileMetadata testMetadata;

    @BeforeEach
    void setUp() {
//        testMetadata = FileMetadata.builder()
//                .id(1L)
//                .fileName("test.txt")
//                .filePath(tempDir.resolve("test.txt").toString())
//                .fileSize(100L)
//                .build();

        testDto = FileDto.builder()
                .id(1L)
                .fileName("test.txt")
                .fileSize(100L)
                .downloadUrl("/api/files/1/download")
                .build();
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testGetFileMetadata_Success() {
        when(fileService.getFileMetadata(1L)).thenReturn(Mono.just(testDto));

        webTestClient.get()
                .uri("/api/v1/files/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.fileName").isEqualTo("test.txt")
                .jsonPath("$.fileSize").isEqualTo(100);
    }

    @Test
    void testGetFileMetadata_Unauthorized() {
        webTestClient.get()
                .uri("/api/v1/files/1")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testDownloadFile_Success() throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, "test content".getBytes());

        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap("test content".getBytes());
        Flux<DataBuffer> dataBufferFlux = Flux.just(dataBuffer);

        when(fileService.getFileMetadata(1L)).thenReturn(Mono.just(testDto));
        when(fileService.downloadFile(1L)).thenReturn(Mono.just(dataBufferFlux));

        webTestClient.get()
                .uri("/api/v1/files/1/download")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .expectHeader().exists("Content-Disposition")
                .expectBody(String.class)
                .isEqualTo("test content");
    }

    @Test
    void testDownloadFile_Unauthorized() {
        webTestClient.get()
                .uri("/api/v1/files/1/download")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testGetFileMetadata_WithAdminRole() {
        when(fileService.getFileMetadata(1L)).thenReturn(Mono.just(testDto));

        webTestClient.get()
                .uri("/api/v1/files/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1);
    }
}
