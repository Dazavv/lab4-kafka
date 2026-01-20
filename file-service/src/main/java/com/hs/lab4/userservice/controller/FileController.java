package com.hs.lab4.userservice.controller;

import com.hs.lab4.userservice.dto.FileDto;
import com.hs.lab4.userservice.dto.requests.AddRoleToUserRequest;
import com.hs.lab4.userservice.dto.requests.RefreshJwtRequest;
import com.hs.lab4.userservice.dto.responses.JwtResponse;
import com.hs.lab4.userservice.enums.Role;
import com.hs.lab4.userservice.mapper.FileMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final FileMapper fileMapper;


    @PostMapping(value = "/upload")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<FileDto> uploadFile(@RequestParam("file") MultipartFile file) {
        return null;
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public ResponseEntity<FileDto> getFileMetadata(@PathVariable Long id) {
        return ResponseEntity.ok(fileService.getFileMetadata(id));
    }
}