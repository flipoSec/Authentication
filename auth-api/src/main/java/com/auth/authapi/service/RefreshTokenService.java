package com.auth.authapi.service;

import com.auth.authapi.entity.RefreshToken;
import com.auth.authapi.entity.User;
import com.auth.authapi.exception.InvalidRefreshTokenException;
import com.auth.authapi.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepo;

    public String generateRefreshToken(User user) throws Exception {
        // random token
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        // hash
        String tokenHash = hashToken(rawToken);

        // store hash in db
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setCreated_at(new Date());
        refreshToken.setExpires_at(
                new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000)
        );
        refreshToken.setTokenHash(tokenHash);

        refreshTokenRepo.save(refreshToken);

        return rawToken;
    }

    public void revokeToken(RefreshToken refreshToken) {

        refreshToken.setRevoked_at(new Date());
        refreshTokenRepo.save(refreshToken);
    }

    public RefreshToken validateRefreshToken(String rawToken) throws Exception {

        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepo
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        validateTokenStatus(refreshToken);

        return refreshToken;
    }

    public RefreshToken validateRefreshTokenWithLock(String rawToken) throws Exception {

        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepo
                .findByTokenHashWithLock(tokenHash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        validateTokenStatus(refreshToken);

        return refreshToken;
    }

    private String hashToken(String rawToken) throws Exception {

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        byte[] hashBytes = messageDigest.digest(
                rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        StringBuilder tokenHash = new StringBuilder();

        for (byte b : hashBytes) {
            tokenHash.append(String.format("%02x", b));
        }

        return tokenHash.toString();
    }

    private void validateTokenStatus(RefreshToken refreshToken) {

        if (refreshToken.getExpires_at().before(new Date())) {
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }

        if (refreshToken.getRevoked_at() != null) {
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }
    }
}
