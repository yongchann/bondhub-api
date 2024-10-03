package com.bondhub.domain.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "users")
@Entity
public class User {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @Id
    private Long id;

    private String username;

    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    private String token;

    private LocalDateTime lastLoginDateTime;

    public void login(String token) {
        this.token = token;
        this.lastLoginDateTime = LocalDateTime.now();
    }

    public void logout() {
        this.token = "";
    }
}
