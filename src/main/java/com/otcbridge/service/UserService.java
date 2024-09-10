package com.otcbridge.service;

import com.otcbridge.domain.User;
import com.otcbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public void register(String username, String password) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(username, hashedPassword);
        userRepository.save(user);
    }

    @Transactional
    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String token = UUID.randomUUID().toString();
        user.login(token);
        return token;
    }

    public boolean validateToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Optional<User> user = userRepository.findByToken(token);
        if (user.isEmpty()) {
            return false;
        }

        LocalDateTime lastLoginDateTime = user.get().getLastLoginDateTime();
        return LocalDateTime.now().minusHours(6).isBefore(lastLoginDateTime);
    }

    @Transactional
    public void logout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        user.logout();
    }
}
