package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.dto.LoginRequest;
import com.startup.tasteflowbe.dto.RegisterRequest;
import com.startup.tasteflowbe.enums.Role;
import com.startup.tasteflowbe.repository.UserRepository;
import com.startup.tasteflowbe.service.AuthService;
import com.startup.tasteflowbe.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // Để mã hóa mật khẩu
    private final JwtUtil jwtUtil;  // Để tạo JWT token
    private final JavaMailSender mailSender;

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
        user.setPhone(registerRequest.getPhone());
        user.setAddress(registerRequest.getAddress());
        user.setEnabled(false);
        user.setVerificationToken(UUID.randomUUID().toString());
        userRepository.save(user);

        sendVerificationEmail(user);
        return jwtUtil.generateToken(user);  // You could return the JWT token for the newly registered user
    }

    @Override
    public void enableUser(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        String resetToken = UUID.randomUUID().toString();
        user.setVerificationToken(resetToken);
        userRepository.save(user);

        String link = "http://localhost:8081/reset-password?token=" + resetToken;
        String message = String.format(
                "Chào %s,\n\n" +
                        "Bạn đã yêu cầu đặt lại mật khẩu. Nhấn vào liên kết sau để tiếp tục:\n%s\n\n" +
                        "Nếu không phải bạn, vui lòng bỏ qua email này.\n\n" +
                        "Trân trọng,\nTasteFlow",
                user.getUsername(), link
        );

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(user.getEmail());
        mail.setSubject("Yêu cầu đặt lại mật khẩu");
        mail.setText(message);
        mailSender.send(mail);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ hoặc đã hết hạn"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setVerificationToken(null); // clear token
        userRepository.save(user);
    }



    private void sendVerificationEmail(User user) {
        String link = "http://localhost:8081/verify-email?token=" + user.getVerificationToken();
        String message = String.format(
                "Chào %s,\n\n" +
                        "Vui lòng xác minh email của bạn bằng cách nhấn vào đường link sau:\n%s\n\n" +
                        "Link này sẽ hết hạn sau 24 giờ.\n\n" +
                        "Nếu bạn không đăng ký tài khoản, vui lòng bỏ qua email này.\n\n" +
                        "Trân trọng,\nTestaFlow",
                user.getUsername(), link);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Xác minh email của bạn");
        mailMessage.setText(message);

        mailSender.send(mailMessage);
    }
}
