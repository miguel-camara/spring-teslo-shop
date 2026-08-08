package com.teslo.shop.auth.service;

import com.teslo.shop.auth.dto.AuthResponse;
import com.teslo.shop.auth.dto.LoginRequest;
import com.teslo.shop.auth.dto.RegisterRequest;
import com.teslo.shop.auth.dto.UserResponse;
import com.teslo.shop.auth.entity.User;
import com.teslo.shop.auth.jwt.JwtService;
import com.teslo.shop.auth.repository.UserRepository;
import com.teslo.shop.common.exception.ApiBadRequestException;
import com.teslo.shop.common.exception.ApiUnauthorizedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setActive(true);
        user.setRoles(new String[] { "user" });

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new ApiBadRequestException("Key (email)=(" + user.getEmail() + ") already exists.");
        }

        return new AuthResponse(UserResponse.from(user), jwtService.generateToken(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ApiUnauthorizedException("Credentials are not valid (email)"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiUnauthorizedException("Credentials are not valid (password)");
        }

        return new AuthResponse(UserResponse.from(user), jwtService.generateToken(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse checkAuthStatus(User user) {
        return new AuthResponse(UserResponse.from(user), jwtService.generateToken(user));
    }
}
