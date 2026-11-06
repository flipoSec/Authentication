package com.auth.authapi.service;


import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
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
}
