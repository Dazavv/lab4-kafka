package com.hs.lab4.fileservice.controller;

import com.hs.lab4.fileservice.dto.FileDto;
import com.hs.lab4.fileservice.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

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
    void testUploadFile_Success() {
        FilePart testFilePart = mock(FilePart.class);
        when(fileService.uploadFile(any(FilePart.class))).thenReturn(Mono.just(testDto));

        StepVerifier.create(fileController.uploadFile(testFilePart))
                .expectNextMatches(response -> {
                    ResponseEntity<FileDto> entity = response;
                    return entity.getStatusCode() == HttpStatus.OK &&
                           entity.getBody() != null &&
                           entity.getBody().getId().equals(1L) &&
                           entity.getBody().getFileName().equals("test.txt");
                })
                .verifyComplete();

        verify(fileService, times(1)).uploadFile(testFilePart);
    }

    @Test
    void testGetFileMetadata_Success() {
        when(fileService.getFileMetadata(1L)).thenReturn(Mono.just(testDto));

        StepVerifier.create(fileController.getFileMetadata(1L))
                .expectNextMatches(response -> {
                    ResponseEntity<FileDto> entity = response;
                    return entity.getStatusCode() == HttpStatus.OK &&
                           entity.getBody() != null &&
                           entity.getBody().getId().equals(1L) &&
                           entity.getBody().getFileName().equals("test.txt");
                })
                .verifyComplete();

        verify(fileService, times(1)).getFileMetadata(1L);
    }

    @Test
    void testDownloadFile_Success() {
        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap("test content".getBytes());
        Flux<DataBuffer> dataBufferFlux = Flux.just(dataBuffer);

        when(fileService.getFileMetadata(1L)).thenReturn(Mono.just(testDto));
        when(fileService.downloadFile(1L)).thenReturn(Mono.just(dataBufferFlux));

        StepVerifier.create(fileController.downloadFile(1L))
                .expectNextMatches(response -> {
                    ResponseEntity<Flux<DataBuffer>> entity = response;
                    return entity.getStatusCode() == HttpStatus.OK &&
                           entity.getHeaders().getContentType() != null &&
                           entity.getHeaders().getContentDisposition() != null &&
                           entity.getBody() != null;
                })
                .verifyComplete();

        verify(fileService, times(1)).getFileMetadata(1L);
        verify(fileService, times(1)).downloadFile(1L);
    }
}
