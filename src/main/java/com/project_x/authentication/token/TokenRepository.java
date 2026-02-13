package com.project_x.authentication.token;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {

    @Query("SELECT t FROM Token t WHERE t.user.id = :userId AND (t.expired = false AND t.revoked = false)")
    List<Token> findAllValidTokensByUser(@Param("userId") UUID userId);

    Optional<Token> findByToken(String token);

    @Transactional
    @Modifying
    @Query("DELETE FROM Token t WHERE t.expiresAt < :now")
    void deleteAllExpiredSince(@Param("now") Instant now);


}
