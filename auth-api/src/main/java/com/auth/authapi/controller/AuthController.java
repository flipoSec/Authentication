package com.auth.authapi.controller;

import com.auth.authapi.dto.LoginRequest;
import com.auth.authapi.dto.RegisterRequest;
import com.auth.authapi.dto.UserResponse;
import com.auth.authapi.entity.User;
import com.auth.authapi.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<UserResponse> loginUser(@Valid @RequestBody LoginRequest request){
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        User loggedUser  = authSrvc.loginUser(user);
        UserResponse userR = new UserResponse(loggedUser.getId(), loggedUser.getName(), loggedUser.getEmail());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userR);
    }


}
