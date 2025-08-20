package com.medihelp360.user.repository;

import com.medihelp360.user.domain.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    
    Optional<UserSession> findByTokenHash(String tokenHash);
    
    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);
    
    List<UserSession> findByUserId(UUID userId);
    
    @Query("SELECT s FROM UserSession s WHERE s.expiresAt < :now")
    List<UserSession> findExpiredSessions(@Param("now") LocalDateTime now);
    
    void deleteByUserId(UUID userId);
    
    void deleteByExpiresAtBefore(LocalDateTime date);
}
