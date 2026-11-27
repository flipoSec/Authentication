package com.auth.authapi.repository;

import com.auth.authapi.entity.User;
import com.auth.authapi.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByTokenHash(String tokenHash);
    List<VerificationToken> findByUser(User user);
}
