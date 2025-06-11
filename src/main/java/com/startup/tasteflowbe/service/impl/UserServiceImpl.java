package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.repository.UserRepository;
import com.startup.tasteflowbe.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

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
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(updatedUser.getUsername());
                    user.setEmail(updatedUser.getEmail());
                    user.setPasswordHash(updatedUser.getPasswordHash());
                    user.setRole(updatedUser.getRole());
                    user.setFirstName(updatedUser.getFirstName());
                    user.setLastName(updatedUser.getLastName());
                    user.setPhone(updatedUser.getPhone());
                    user.setAddress(updatedUser.getAddress());
                    user.setPoints(updatedUser.getPoints());
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
    public List<User> getAvailableShopManagers() { return userRepository.findAvailableShopManagers(); }
}
