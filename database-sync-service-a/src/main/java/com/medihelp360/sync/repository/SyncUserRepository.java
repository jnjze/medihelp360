package com.medihelp360.sync.repository;

import com.medihelp360.sync.domain.SyncUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SyncUserRepository extends JpaRepository<SyncUser, UUID> {
    
    Optional<SyncUser> findByOriginalUserId(UUID originalUserId);
    
    List<SyncUser> findByUserStatus(String userStatus);
    
    List<SyncUser> findByIsActive(Boolean isActive);
    
    @Query("SELECT s FROM SyncUser s WHERE s.syncedAt BETWEEN :startDate AND :endDate")
    List<SyncUser> findBySyncedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s FROM SyncUser s WHERE s.userEmail LIKE %:email%")
    List<SyncUser> findByUserEmailContaining(@Param("email") String email);
    
    @Query("SELECT COUNT(s) FROM SyncUser s WHERE s.userStatus = :status")
    Long countByUserStatus(@Param("status") String status);
    
    @Query("SELECT COUNT(s) FROM SyncUser s WHERE s.isActive = true")
    Long countActiveUsers();
    
    boolean existsByOriginalUserId(UUID originalUserId);
} 