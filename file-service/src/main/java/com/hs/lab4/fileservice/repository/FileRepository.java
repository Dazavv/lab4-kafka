package com.hs.lab4.fileservice.repository;

import com.hs.lab4.fileservice.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileMetadata, Long> {
}
