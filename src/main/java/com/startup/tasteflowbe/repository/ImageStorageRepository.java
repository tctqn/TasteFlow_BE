package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.ImageStorage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageStorageRepository extends JpaRepository<ImageStorage, Long> {
}
