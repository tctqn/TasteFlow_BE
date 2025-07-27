package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    @Query("SELECT u FROM User u WHERE u.role = com.startup.tasteflowbe.enums.Role.WAREHOUSE_MANAGER AND u.warehouse IS NULL")
    List<User> findAvailableWarehouseManagers();
    @Query("SELECT u FROM User u WHERE u.role = com.startup.tasteflowbe.enums.Role.STORE_MANAGER AND u.store IS NULL")
    List<User> findAvailableStoreManagers();
    User findByUserId(Long userId);
    Optional<User> findByVerificationToken(String token);
    Optional<User> findByEmail(String email);
    @Query("SELECT u FROM User u JOIN StoreStaff ss ON u = ss.user WHERE ss.store.storeId = :storeId AND ss.active = true")
    List<User> findActiveUsersByStoreId(Long storeId);
}
