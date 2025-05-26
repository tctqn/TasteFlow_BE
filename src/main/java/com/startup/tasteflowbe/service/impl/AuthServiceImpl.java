package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.dto.LoginRequest;
import com.startup.tasteflowbe.dto.RegisterRequest;
import com.startup.tasteflowbe.enums.Role;
import com.startup.tasteflowbe.repository.UserRepository;
import com.startup.tasteflowbe.service.AuthService;
import com.startup.tasteflowbe.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // Để mã hóa mật khẩu
    private final JwtUtil jwtUtil;  // Để tạo JWT token

    @Override
    public String login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        return jwtUtil.generateToken(user);  // Generate JWT token after successful login
    }

    @Override
    public String register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already taken");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setRole(Role.CUSTOMER);  // Set default role, assuming you have an enum Role

        userRepository.save(user);
        return jwtUtil.generateToken(user);  // You could return the JWT token for the newly registered user
    }

}
