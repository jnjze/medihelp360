package com.medihelp360.user.repository;

import com.medihelp360.user.domain.FailedLoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FailedLoginAttemptRepository extends JpaRepository<FailedLoginAttempt, UUID> {
    
    Optional<FailedLoginAttempt> findByEmailAndIpAddress(String email, String ipAddress);
    
    List<FailedLoginAttempt> findByEmail(String email);
    
    List<FailedLoginAttempt> findByIpAddress(String ipAddress);
    
    @Query("SELECT f FROM FailedLoginAttempt f WHERE f.blockedUntil IS NOT NULL AND f.blockedUntil > :now")
    List<FailedLoginAttempt> findCurrentlyBlocked(@Param("now") LocalDateTime now);
    
    @Query("SELECT f FROM FailedLoginAttempt f WHERE f.lastAttemptAt < :cutoffDate")
    List<FailedLoginAttempt> findOldAttempts(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Modifying
    @Query("DELETE FROM FailedLoginAttempt f WHERE f.email = :email AND f.ipAddress = :ipAddress")
    void deleteByEmailAndIpAddress(@Param("email") String email, @Param("ipAddress") String ipAddress);
    
    @Modifying
    @Query("DELETE FROM FailedLoginAttempt f WHERE f.lastAttemptAt < :cutoffDate")
    void deleteOldAttempts(@Param("cutoffDate") LocalDateTime cutoffDate);
}
