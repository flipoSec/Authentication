package com.auth.authapi.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "refresh_token")
@Data
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private Date expires_at;
    @Column(nullable = false, updatable = false)
    private Date created_at;

    private Date revoked_at;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


}
