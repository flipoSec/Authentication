package com.auth.authapi.service;

import com.auth.authapi.entity.User;
import com.auth.authapi.entity.VerificationToken;
import com.auth.authapi.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Service
public class VerificationTokenService {

    @Autowired
    private VerificationTokenRepository tokenRepo;

    private final SecureRandom secureRandom = new SecureRandom();

    public String createVerificationToken(User user) {

        // Generate random token
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        // Hash the token
        String tokenHash = hashToken(rawToken);

        // Set exp to 15 min
        Date expiryDate = Date.from(
                Instant.now().plus(15, ChronoUnit.MINUTES)
        );

        // Create VerificationToken object
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setTokenHash(tokenHash);
        verificationToken.setExpiryDate(expiryDate);
        verificationToken.setUsed(false);
        verificationToken.setUser(user);

        // Save token in db
        tokenRepo.save(verificationToken);

        // Return raw token for the email link
        return rawToken;
    }

    public VerificationToken validateVerificationToken(String rawToken) {

        // Hash the received token
        String tokenHash = hashToken(rawToken);

        // Find token in database
        VerificationToken verificationToken = tokenRepo
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid verification token"
                ));

        // Check if token was already used
        if (verificationToken.isUsed()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Verification token already used"
            );
        }

        // Check expiration
        if (verificationToken.getExpiryDate().before(new Date())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Verification token expired"
            );
        }

        return verificationToken;
    }

    public void markTokenAsUsed(VerificationToken verificationToken) {
        verificationToken.setUsed(true);
        tokenRepo.save(verificationToken);
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(
                    token.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (Exception e) {
            throw new IllegalStateException("Could not hash verification token", e);
        }
    }
}
