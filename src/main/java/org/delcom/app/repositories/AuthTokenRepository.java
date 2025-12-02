package org.delcom.app.repositories;

import org.delcom.app.entities.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {
    Optional<AuthToken> findByToken(String token);
    Optional<AuthToken> findByUserId(UUID userId);
    void deleteByToken(String token);
}

