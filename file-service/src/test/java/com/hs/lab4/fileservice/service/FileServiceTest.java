package com.hs.lab4.fileservice.service;

import com.hs.lab4.fileservice.config.FileStorageConfig;
import com.hs.lab4.fileservice.dto.FileDto;
import com.hs.lab4.fileservice.entity.FileMetadata;
import com.hs.lab4.fileservice.exceptions.FileNotFoundException;
import com.hs.lab4.fileservice.exceptions.FileStorageException;
import com.hs.lab4.fileservice.mapper.FileMapper;
import com.hs.lab4.fileservice.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

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
    }

    @Test
    void testGetFileMetadata_Success() {
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testMetadata));
        when(fileMapper.toDto(testMetadata)).thenReturn(testDto);

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
        Path nonExistentPath = testUploadPath.resolve("non-existent.txt");
        testMetadata.setFilePath(nonExistentPath.toString());
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testMetadata));

        StepVerifier.create(fileService.downloadFile(1L))
                .expectError(FileNotFoundException.class)
                .verify();

        verify(fileRepository, times(1)).findById(1L);
    }

    @Test
    void testUploadFile_Success() throws Exception {
        when(fileStorageConfig.getUploadPath()).thenReturn(testUploadPath);
        FilePart filePart = mock(FilePart.class);
        when(filePart.filename()).thenReturn("test.txt");
        
        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap("test content".getBytes());
        Flux<DataBuffer> content = Flux.just(dataBuffer);
        when(filePart.content()).thenReturn(content);

        FileMetadata savedMetadata = FileMetadata.builder()
                .id(1L)
                .fileName("test.txt")
                .filePath(testUploadPath.resolve("uuid.txt").toString())
                .fileSize(0L)
                .build();

        FileMetadata updatedMetadata = FileMetadata.builder()
                .id(1L)
                .fileName("test.txt")
                .filePath(testUploadPath.resolve("uuid.txt").toString())
                .fileSize(12L) // "test content".length()
                .build();

        when(fileRepository.save(any(FileMetadata.class))).thenReturn(savedMetadata, updatedMetadata);
        when(fileMapper.toDto(updatedMetadata)).thenReturn(testDto);

        StepVerifier.create(fileService.uploadFile(filePart))
                .expectNextMatches(dto -> 
                    dto != null &&
                    dto.getId().equals(1L) &&
                    dto.getFileName().equals("test.txt")
                )
                .verifyComplete();

        verify(fileRepository, atLeastOnce()).save(any(FileMetadata.class));
        verify(fileMapper, times(1)).toDto(updatedMetadata);
    }

    @Test
    void testUploadFile_StorageException() {
        FilePart filePart = mock(FilePart.class);
        lenient().when(filePart.filename()).thenReturn("test.txt");
        
        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap("test content".getBytes());
        Flux<DataBuffer> content = Flux.just(dataBuffer);
        lenient().when(filePart.content()).thenReturn(content);

        Path invalidPath = Path.of("/invalid/path/that/cannot/be/created");
        when(fileStorageConfig.getUploadPath()).thenReturn(invalidPath);

        StepVerifier.create(fileService.uploadFile(filePart))
                .expectError(FileStorageException.class)
                .verify();
    }
}
