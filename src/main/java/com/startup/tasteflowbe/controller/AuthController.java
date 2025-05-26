package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.dto.LoginRequest;
import com.startup.tasteflowbe.dto.RegisterRequest;
import com.startup.tasteflowbe.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;  // Sử dụng AuthService để xử lý đăng nhập, đăng ký, đăng xuất

    // Đăng ký người dùng mới
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // Gọi phương thức register của AuthService
            String token = authService.register(registerRequest);
            return ResponseEntity.status(201).body(token);  // Trả về JWT token sau khi đăng ký thành công
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());  // Nếu có lỗi như username hoặc email đã tồn tại
        }
    }

    // Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Gọi phương thức login của AuthService
            String token = authService.login(loginRequest);
            return ResponseEntity.ok(token);  // Trả về JWT token sau khi đăng nhập thành công
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());  // Nếu thông tin đăng nhập không hợp lệ
        }
    }
}
