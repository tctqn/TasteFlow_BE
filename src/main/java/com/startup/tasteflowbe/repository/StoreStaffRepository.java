package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.StoreStaff;
import com.startup.tasteflowbe.model.Store;
import com.startup.tasteflowbe.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StoreStaffRepository extends JpaRepository<StoreStaff, Long> {
    boolean existsByUserAndActiveTrue(User user);
}

