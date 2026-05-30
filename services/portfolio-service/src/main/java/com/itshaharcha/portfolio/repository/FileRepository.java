package com.itshaharcha.portfolio.repository;

import com.itshaharcha.portfolio.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileRepository extends JpaRepository<StoredFile, UUID> {
}
