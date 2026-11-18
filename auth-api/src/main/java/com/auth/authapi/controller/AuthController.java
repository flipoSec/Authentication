package com.auth.authapi.controller;

import com.auth.authapi.dto.*;
import com.auth.authapi.entity.RefreshToken;
import com.auth.authapi.entity.User;
import com.auth.authapi.service.AuthService;
import com.auth.authapi.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authSrvc;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest request){
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        User registerdUSer = authSrvc.registerUser(user);
        UserResponse userR = new UserResponse(registerdUSer.getId(), registerdUSer.getName(), registerdUSer.getEmail());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userR);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(Authentication authentication) throws Exception {
        AuthResponse authRes = authSrvc.loginUser(authentication.getName());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authRes);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> userProfile(Authentication authentication){
        String email = authentication.getName();
        User user = authSrvc.getUserProfile(email);
        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(userResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> createRefreshToken(@Valid @RequestBody RefreshRequest request) throws Exception {
        AuthResponse authRes = authSrvc.refreshAccessToken(
                request.getRefreshToken()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authRes);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(Authentication auth,
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody LogoutRequest request) throws Exception {

        String accessToken = authorizationHeader.substring(7);
        authSrvc.logoutUser(
                auth.getName(),
                accessToken,
                request.getRefreshToken()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
