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
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = FileController.class)
@ContextConfiguration(classes = FileServiceApplication.class)
@Import(TestSecurityConfig.class)
class FileControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private FileService fileService;

    @TempDir
    Path tempDir;

    private FileDto testDto;

    @BeforeEach
    void setUp() {
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

    @Test
    @WithMockUser(authorities = {"USER"})
    void testUploadFile_Success() {
        when(fileService.uploadFile(any())).thenReturn(Mono.just(testDto));

        org.springframework.http.client.MultipartBodyBuilder builder = new org.springframework.http.client.MultipartBodyBuilder();
        builder.part("file", "test content".getBytes())
                .header("Content-Disposition", "form-data; name=\"file\"; filename=\"test.txt\"");

        webTestClient
                .post()
                .uri("/api/v1/files/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.fileName").isEqualTo("test.txt")
                .jsonPath("$.fileSize").isEqualTo(100);

        verify(fileService, times(1)).uploadFile(any());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testUploadFile_WithAdminRole() {
        when(fileService.uploadFile(any())).thenReturn(Mono.just(testDto));

        org.springframework.http.client.MultipartBodyBuilder builder = new org.springframework.http.client.MultipartBodyBuilder();
        builder.part("file", "test content".getBytes())
                .header("Content-Disposition", "form-data; name=\"file\"; filename=\"test.txt\"");

        webTestClient
                .post()
                .uri("/api/v1/files/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1);

        verify(fileService, times(1)).uploadFile(any());
    }

    @Test
    void testUploadFile_Unauthorized() {
        webTestClient
                .post()
                .uri("/api/v1/files/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .exchange()
                .expectStatus().isUnauthorized();

        verify(fileService, never()).uploadFile(any());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testDownloadFile_WithAdminRole() throws Exception {
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
}
