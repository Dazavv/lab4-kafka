package com.hs.lab4.userservice.repository;

import com.hs.lab4.userservice.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileMetadata, Long> {
}
