package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.dto.LoginRequest;
import com.startup.tasteflowbe.dto.RegisterRequest;
import com.startup.tasteflowbe.dto.response.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequest loginRequest);
    String register(RegisterRequest registerRequest);
    void enableUser(String token);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}
