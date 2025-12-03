package org.delcom.app.services;

import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.repositories.AuthTokenRepository;
import org.delcom.app.repositories.UserRepository;
import org.delcom.app.utils.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, AuthTokenRepository authTokenRepository) {
        this.userRepository = userRepository;
        this.authTokenRepository = authTokenRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User register(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email sudah terdaftar");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public AuthToken login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Email atau password salah");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Email atau password salah");
        }

        // Delete existing token
        authTokenRepository.findByUserId(user.getId()).ifPresent(authTokenRepository::delete);

        // Create new token
        String token = JwtUtil.generateToken(user.getId());
        AuthToken authToken = new AuthToken();
        authToken.setToken(token);
        authToken.setUserId(user.getId());
        return authTokenRepository.save(authToken);
    }

    public Optional<User> getUserByToken(String token) {
        if (!JwtUtil.validateToken(token, false)) {
            return Optional.empty();
        }

        UUID userId = JwtUtil.extractUserId(token);
        if (userId == null) {
            return Optional.empty();
        }
        return userRepository.findById(userId);
    }

    @Transactional
    public void logout(String token) {
        authTokenRepository.deleteByToken(token);
    }

    public Optional<User> getUserById(UUID userId) {
        return userRepository.findById(userId);
    }
}

