package com.auth.authapi.service;

import com.auth.authapi.dto.AuthResponse;
import com.auth.authapi.entity.User;
import com.auth.authapi.exception.EmailAlreadyExistsException;
import com.auth.authapi.exception.InvalidCredentialsException;
import com.auth.authapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class AuthService {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passEncoder;

    @Autowired
    private JWTService jwtSrvc;

    public User registerUser(User user){
        if(userRepo.existsByEmail(user.getEmail())){
            throw new EmailAlreadyExistsException("Email already exist");
        }
        String hashedPass = passEncoder.encode(user.getPassword());
        user.setPassword(hashedPass);
        return userRepo.save(user);

    }

    public AuthResponse loginUser(String email) {
        String token = jwtSrvc.generateToken(email);
        return new AuthResponse(token);
    }

}
