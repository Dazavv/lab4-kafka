package com.hs.lab4.fileservice.controller;

import com.hs.lab4.fileservice.dto.FileDto;
import com.hs.lab4.fileservice.service.FileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public Mono<ResponseEntity<FileDto>> uploadFile(@RequestPart("file") FilePart filePart) {
        return fileService.uploadFile(filePart)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public Mono<ResponseEntity<FileDto>> getFileMetadata(@PathVariable Long id) {
        return fileService.getFileMetadata(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public Mono<ResponseEntity<Flux<DataBuffer>>> downloadFile(@PathVariable Long id) {
        return fileService.getFileMetadata(id)
                .flatMap(metadata -> fileService.downloadFile(id)
                        .map(dataBufferFlux -> {
                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                            headers.setContentDispositionFormData("attachment", metadata.getFileName());
                            return ResponseEntity.ok()
                                    .headers(headers)
                                    .body(dataBufferFlux);
                        }));
    }
}