package com.hs.lab4.fileservice.service;

import com.hs.lab4.userservice.config.FileStorageConfig;
import com.hs.lab4.userservice.dto.FileDto;
import com.hs.lab4.userservice.entity.FileMetadata;
import com.hs.lab4.userservice.exceptions.FileNotFoundException;
import com.hs.lab4.userservice.mapper.FileMapper;
import com.hs.lab4.userservice.repository.FileRepository;
import com.hs.lab4.userservice.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FileMapper fileMapper;

    @Mock
    private FileStorageConfig fileStorageConfig;

    @InjectMocks
    private FileService fileService;

    @TempDir
    Path tempDir;

    private FileMetadata testMetadata;
    private FileDto testDto;
    private Path testUploadPath;

    @BeforeEach
    void setUp() {
        testUploadPath = tempDir.resolve("uploads");
        when(fileStorageConfig.getUploadPath()).thenReturn(testUploadPath);

        testMetadata = FileMetadata.builder()
                .id(1L)
                .fileName("test.txt")
                .filePath(testUploadPath.resolve("test-uuid.txt").toString())
                .fileSize(100L)
                .build();

        testDto = FileDto.builder()
                .id(1L)
                .fileName("test.txt")
                .fileSize(100L)
                .downloadUrl("/api/files/1/download")
                .build();

        when(fileMapper.toDto(any(FileMetadata.class))).thenReturn(testDto);
    }

    @Test
    void testGetFileMetadata_Success() {
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testMetadata));

        StepVerifier.create(fileService.getFileMetadata(1L))
                .expectNext(testDto)
                .verifyComplete();

        verify(fileRepository, times(1)).findById(1L);
        verify(fileMapper, times(1)).toDto(testMetadata);
    }

    @Test
    void testGetFileMetadata_NotFound() {
        when(fileRepository.findById(1L)).thenReturn(Optional.empty());

        StepVerifier.create(fileService.getFileMetadata(1L))
                .expectError(FileNotFoundException.class)
                .verify();

        verify(fileRepository, times(1)).findById(1L);
        verify(fileMapper, never()).toDto(any());
    }

    @Test
    void testDownloadFile_Success() throws Exception {
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testMetadata));

        Path testFilePath = testUploadPath.resolve("test-file.txt");
        Files.createDirectories(testFilePath.getParent());
        Files.write(testFilePath, "test content".getBytes());
        
        testMetadata.setFilePath(testFilePath.toString());
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testMetadata));

        StepVerifier.create(fileService.downloadFile(1L))
                .expectNextCount(1) // Flux
                .verifyComplete();

        verify(fileRepository, times(1)).findById(1L);
    }

    @Test
    void testDownloadFile_NotFound() {
        when(fileRepository.findById(1L)).thenReturn(Optional.empty());

        StepVerifier.create(fileService.downloadFile(1L))
                .expectError(FileNotFoundException.class)
                .verify();

        verify(fileRepository, times(1)).findById(1L);
    }

    @Test
    void testDownloadFile_FileNotOnDisk() {
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testMetadata));
        
        Path nonExistentPath = testUploadPath.resolve("non-existent.txt");
        testMetadata.setFilePath(nonExistentPath.toString());
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testMetadata));

        StepVerifier.create(fileService.downloadFile(1L))
                .expectError(FileNotFoundException.class)
                .verify();

        verify(fileRepository, times(1)).findById(1L);
    }
}
