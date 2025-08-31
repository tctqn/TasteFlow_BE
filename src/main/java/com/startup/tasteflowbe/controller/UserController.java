package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available-managers")
    public ResponseEntity<List<User>> getAvailableWarehouseManagers() {
        return ResponseEntity.ok(userService.getAvailableWarehouseManagers());
    }

    @GetMapping("/available-store-managers")
    public ResponseEntity<List<User>> getAvailableStoreManagers() {
        return ResponseEntity.ok(userService.getAvailableStoreManagers());
    }

    @GetMapping("/active-by-store/{storeId}")
    public ResponseEntity<List<User>> getActiveUsersByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(userService.getActiveUsersByStoreId(storeId));
    }

    @GetMapping("/{id}/points")
    public ResponseEntity<Integer> getPointsByUserId(@PathVariable Long id) {
        Integer points = userService.getPointByUserId(id);
        if (points == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userService.getPointByUserId(id));
    }
}
