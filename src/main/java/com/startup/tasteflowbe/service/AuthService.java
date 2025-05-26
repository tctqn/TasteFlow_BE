package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.dto.LoginRequest;
import com.startup.tasteflowbe.dto.RegisterRequest;

public interface AuthService {
    String login(LoginRequest loginRequest);
    String register(RegisterRequest registerRequest);
}
