package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.ProductUnit;
import com.startup.tasteflowbe.model.ReturnAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnAttachmentRepository extends JpaRepository<ReturnAttachment, Long> {
}
