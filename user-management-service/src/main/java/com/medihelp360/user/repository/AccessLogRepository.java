package com.medihelp360.user.repository;

import com.medihelp360.user.domain.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, UUID> {
    
    List<AccessLog> findByUserIdOrderByTimestampDesc(UUID userId);
    
    List<AccessLog> findByActionOrderByTimestampDesc(String action);
    
    List<AccessLog> findBySuccessOrderByTimestampDesc(Boolean success);
    
    @Query("SELECT a FROM AccessLog a WHERE a.timestamp BETWEEN :startDate AND :endDate")
    List<AccessLog> findByTimestampBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM AccessLog a WHERE a.ipAddress = :ipAddress ORDER BY a.timestamp DESC")
    List<AccessLog> findByIpAddressOrderByTimestampDesc(@Param("ipAddress") String ipAddress);
    
    @Query("SELECT COUNT(a) FROM AccessLog a WHERE a.userId = :userId AND a.action = :action AND a.timestamp >= :since")
    Long countByUserIdAndActionSince(@Param("userId") UUID userId, 
                                   @Param("action") String action, 
                                   @Param("since") LocalDateTime since);
}
