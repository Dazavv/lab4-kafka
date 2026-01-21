package com.hs.lab4.fileservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDto {
    private Long id;
    private String fileName;
    private Long fileSize;
    private String downloadUrl;
}
