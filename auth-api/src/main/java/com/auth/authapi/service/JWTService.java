package com.auth.authapi.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import java.util.Date;

@Service
public class JWTService {

    @Value("${JWT_SECRET}")
    private String secretKey;

    public SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email) {

        Date now = new Date();

        Date expiration = new Date(now.getTime() + 86400000);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }


    public Date extractExpiration(String token){
        return extractAllClaims(token).getExpiration();
    }

    public Boolean validateToken(String token, UserDetails userDetails){
        try {
            final String email = extractEmail(token);
            return email.equals(userDetails.getUsername())
                    && userDetails.isEnabled()
                    && userDetails.isAccountNonExpired()
                    && userDetails.isAccountNonLocked()
                    && userDetails.isCredentialsNonExpired();
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
