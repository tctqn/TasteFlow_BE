package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.repository.UserRepository;
import com.startup.tasteflowbe.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User createUser(User user) {
        // Check for duplicate username
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        // Check for duplicate email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    // If role is changed, only set role and return
                    if (!user.getRole().equals(updatedUser.getRole())) {
                        user.setRole(updatedUser.getRole());
                        return userRepository.save(user);
                    }
                    // Check for duplicate username (if changed)
                    if (!user.getUsername().equals(updatedUser.getUsername()) && userRepository.existsByUsername(updatedUser.getUsername())) {
                        throw new IllegalArgumentException("Username already exists");
                    }
                    // Check for duplicate email (if changed)
                    if (!user.getEmail().equals(updatedUser.getEmail()) && userRepository.existsByEmail(updatedUser.getEmail())) {
                        throw new IllegalArgumentException("Email already exists");
                    }
                    user.setEmail(updatedUser.getEmail());
                    user.setFirstName(updatedUser.getFirstName());
                    user.setLastName(updatedUser.getLastName());
                    user.setPhone(updatedUser.getPhone());
                    user.setAddress(updatedUser.getAddress());
                    user.setEnabled(updatedUser.isEnabled());
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> getAvailableWarehouseManagers() {
        return userRepository.findAvailableWarehouseManagers();
    }

    @Override
    public List<User> getAvailableStoreManagers() { return userRepository.findAvailableStoreManagers(); }

    @Override
    public List<User> getActiveUsersByStoreId(Long storeId) {
        return userRepository.findActiveUsersByStoreId(storeId);
    }

    @Override
    public Integer getPointByUserId(Long userId) {
        return userRepository.findPointByUserId(userId);
    }
}
