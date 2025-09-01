package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    List<User> getAvailableWarehouseManagers();
    List<User> getAvailableStoreManagers();
    List<User> getActiveUsersByStoreId(Long storeId);
    Integer getPointByUserId(Long userId);
    Integer getPointUsedByUserId(Long userId);
}
