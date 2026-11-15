package com.auth.authapi.service;

import com.auth.authapi.dto.AuthResponse;
import com.auth.authapi.entity.RefreshToken;
import com.auth.authapi.entity.User;
import com.auth.authapi.enums.Role;
import com.auth.authapi.exception.EmailAlreadyExistsException;
import com.auth.authapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @Autowired
    private  RefreshTokenService refreshTokenService;

    public User registerUser(User user){
        if(userRepo.existsByEmail(user.getEmail())){
            throw new EmailAlreadyExistsException("Email already exist");
        }
        String hashedPass = passEncoder.encode(user.getPassword());
        user.setPassword(hashedPass);
        user.setRole(Role.USER);
        return userRepo.save(user);

    }

    public AuthResponse loginUser(String email) throws Exception {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        String accessToken = jwtSrvc.generateToken(email);
        String refreshToken = refreshTokenService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    public User getUserProfile(String email){
        Optional<User> user = userRepo.findByEmail(email);
        if(user.isPresent()){
            return user.get();
        }
        throw new UsernameNotFoundException("User Not Found");
    }

    public AuthResponse refreshAccessToken(String rawToken) throws Exception {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(rawToken);
        User user = refreshToken.getUser();
        String accessToken = jwtSrvc.generateToken(user.getEmail());

        return new AuthResponse(accessToken, rawToken);
    }

}
