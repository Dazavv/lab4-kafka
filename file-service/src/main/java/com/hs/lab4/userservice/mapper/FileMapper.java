package com.hs.lab4.userservice.mapper;

import com.hs.lab4.userservice.dto.FileDto;
import com.hs.lab4.userservice.entity.FileMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FileMapper {
    @Mapping(target = "downloadUrl", expression = "java(\"/api/files/\" + fileMetadata.getId() + \"/download\")")
    FileDto toDto(FileMetadata fileMetadata);
}
