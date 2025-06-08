package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.ImageStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageStorageRepository extends JpaRepository<ImageStorage, Long> {
    Optional<ImageStorage> findFirstByReferenceTableAndReferenceId(String referenceTable, Long referenceId);
    List<ImageStorage> findAllByReferenceTableAndReferenceId(String referenceTable, Long referenceId);
}
