package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.dto.ForgotPasswordRequest;
import com.startup.tasteflowbe.dto.LoginRequest;
import com.startup.tasteflowbe.dto.RegisterRequest;
import com.startup.tasteflowbe.dto.ResetPasswordRequest;
import com.startup.tasteflowbe.dto.response.AuthResponseDTO;
import com.startup.tasteflowbe.dto.response.UserDTO;
import com.startup.tasteflowbe.mapper.UserMapper;
import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;  // Sử dụng AuthService để xử lý đăng nhập, đăng ký, đăng xuất
    private final UserMapper userMapper;  // Mapper để chuyển đổi giữa User và UserDTO

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
//    @PostMapping("/login")
//    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
//        try {
//            // Gọi phương thức login của AuthService
//            String token = authService.login(loginRequest);
//            return ResponseEntity.ok(token);  // Trả về JWT token sau khi đăng nhập thành công
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());  // Nếu thông tin đăng nhập không hợp lệ
//        }
//    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            AuthResponseDTO response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        authService.enableUser(token);
        return ResponseEntity.ok("Email verified successfully!");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request.getEmail());
            return ResponseEntity.ok("Email đặt lại mật khẩu đã được gửi.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok("Mật khẩu đã được đặt lại thành công.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
