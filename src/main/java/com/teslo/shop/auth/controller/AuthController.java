package com.teslo.shop.auth.controller;

import com.teslo.shop.auth.RoleGuard;
import com.teslo.shop.auth.dto.AuthResponse;
import com.teslo.shop.auth.dto.LoginRequest;
import com.teslo.shop.auth.dto.RegisterRequest;
import com.teslo.shop.auth.entity.User;
import com.teslo.shop.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/check-status")
    public AuthResponse checkAuthStatus(@AuthenticationPrincipal User user) {
        return authService.checkAuthStatus(user);
    }

    @GetMapping("/private")
    public Map<String, Object> testingPrivateRoute(@AuthenticationPrincipal User user, HttpServletRequest request) {
        List<String> rawHeaders = new ArrayList<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            rawHeaders.add(name);
            rawHeaders.add(request.getHeader(name));
        }

        Map<String, Object> headers = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            headers.put(name, request.getHeader(name));
        }

        return Map.of(
                "ok", true,
                "message", "Hola Mundo Private",
                "user", user,
                "userEmail", user.getEmail(),
                "rawHeaders", rawHeaders,
                "headers", headers);
    }

    @GetMapping("/private2")
    public Map<String, Object> privateRoute2(@AuthenticationPrincipal User user) {
        RoleGuard.requireAny(user, "super-user", "admin");
        return Map.of("ok", true, "user", user);
    }

    @GetMapping("/private3")
    public Map<String, Object> privateRoute3(@AuthenticationPrincipal User user) {
        RoleGuard.requireAny(user, "admin");
        return Map.of("ok", true, "user", user);
    }
}
