package com.auth.authapi.service;

import com.auth.authapi.dto.AuthResponse;
import com.auth.authapi.dto.ChangePasswordRequest;
import com.auth.authapi.entity.RefreshToken;
import com.auth.authapi.entity.User;
import com.auth.authapi.entity.VerificationToken;
import com.auth.authapi.enums.Role;
import com.auth.authapi.exception.EmailAlreadyExistsException;
import com.auth.authapi.exception.InvalidRefreshTokenException;
import com.auth.authapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.MessagingException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.UnsupportedEncodingException;
import java.util.Date;
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

    @Autowired
    private RedisService redisSrvc;

    @Autowired
    private VerificationTokenService verificationTokenService;

    @Autowired
    private EmailService emailService;

    @Value("${app.base-url}")
    private String appBaseUrl;


    @Transactional(rollbackFor = Exception.class)
    public User registerUser(User user){
        if(userRepo.existsByEmail(user.getEmail())){
            throw new EmailAlreadyExistsException("Email already exist");
        }
        String hashedPass = passEncoder.encode(user.getPassword());
        user.setPassword(hashedPass);
        user.setRole(Role.USER);
        user.setEnabled(false);
        User savedUser = userRepo.save(user);

        // Generate verification token
        String rawToken = verificationTokenService.createVerificationToken(savedUser);

        // Create verification URL
        String verificationUrl = appBaseUrl
                + "/api/auth/verify?token="
                + rawToken;

        // Send verification email
        try {
            emailService.sendVerificationEmail(
                    savedUser.getEmail(),
                    verificationUrl
            );
        } catch (MessagingException | UnsupportedEncodingException | jakarta.mail.MessagingException e) {
            throw new IllegalStateException("Failed to send verification email", e);
        }

        return savedUser;
    }

    public AuthResponse loginUser(String email) throws Exception {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        String accessToken = jwtSrvc.generateToken(email, user.getTokenVersion());
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

    @Transactional(rollbackFor = Exception.class)
    public AuthResponse refreshAccessToken(String rawToken) throws Exception {
        // Validate old refresh token with database lock
        RefreshToken refreshToken = refreshTokenService.validateRefreshTokenWithLock(rawToken);
        User user = refreshToken.getUser();

        // Revoke old refresh token
        refreshTokenService.revokeToken(refreshToken);

        // Generate new refresh token
        String newRefreshToken = refreshTokenService.generateRefreshToken(user);

        // Generate new access token
        String newAccessToken = jwtSrvc.generateToken(user.getEmail(), user.getTokenVersion());

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    public void logoutUser(String email, String accessToken, String rawRefreshToken) throws Exception {
        // validate refresh token
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(rawRefreshToken);

        // check if refresh token belongs to authenticated user
        if (email.equals(refreshToken.getUser().getEmail())) {
            // extract access token information
            String jti = jwtSrvc.extractJti(accessToken);
            Date expiration = jwtSrvc.extractExpiration(accessToken);

            // revoke refresh token in PostgreSQL
            refreshTokenService.revokeToken(refreshToken);

            // revoke access token in Redis
            redisSrvc.revokeToken(jti, expiration);
        }
        else{
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public void changePassword( String email, ChangePasswordRequest request) throws Exception {

        // Find user
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        // Verify current password
        if (passEncoder.matches(request.getCurrentPassword(), user.getPassword()) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        // Verify new password confirmation
        if (request.getNewPassword().equals(request.getConfirmNewPassword()) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password confirmation does not match");
        }

        // Prevent using the same password
        if (passEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from current password");
        }

        // Hash new password
        user.setPassword(passEncoder.encode(request.getNewPassword()));

        // Increment token version to invalidate old access tokens
        user.setTokenVersion(user.getTokenVersion() + 1);

        // Update new password
        userRepo.save(user);

        // Revoke all old refresh tokens
        refreshTokenService.revokeAllUserTokens(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void verifyEmail(String rawToken) {

        // Validate verification token
        VerificationToken verificationToken =
                verificationTokenService.validateVerificationToken(rawToken);

        // Get user linked to the token
        User user = verificationToken.getUser();

        // Enable user account
        user.setEnabled(true);
        userRepo.save(user);

        // Mark token as used
        verificationTokenService.markTokenAsUsed(verificationToken);
    }

}
