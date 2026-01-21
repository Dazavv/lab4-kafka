package com.hs.lab4.fileservice.mapper;

import com.hs.lab4.fileservice.dto.FileDto;
import com.hs.lab4.fileservice.entity.FileMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FileMapper {
    @Mapping(target = "downloadUrl", expression = "java(\"/api/files/\" + fileMetadata.getId() + \"/download\")")
    FileDto toDto(FileMetadata fileMetadata);
}
