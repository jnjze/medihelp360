package com.medihelp360.sync.repository;

import com.medihelp360.sync.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones con entidades de usuario en MySQL
 */
@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {
    
    /**
     * Busca un usuario por su ID original
     */
    Optional<UserEntity> findByOriginalId(Long originalId);
    
    /**
     * Busca usuarios por email
     */
    Optional<UserEntity> findByEmail(String email);
    
    /**
     * Busca usuarios por username
     */
    Optional<UserEntity> findByUsername(String username);
    
    /**
     * Busca usuarios por rol
     */
    List<UserEntity> findByRole(String role);
    
    /**
     * Busca usuarios activos
     */
    List<UserEntity> findByActiveTrue();
    
    /**
     * Busca usuarios por departamento
     */
    List<UserEntity> findByDepartment(String department);
    
    /**
     * Busca usuarios por especialización
     */
    List<UserEntity> findBySpecialization(String specialization);
    
    /**
     * Busca usuarios por estado de sincronización
     */
    List<UserEntity> findBySyncStatus(UserEntity.SyncStatus syncStatus);
    
    /**
     * Busca usuarios sincronizados después de una fecha
     */
    List<UserEntity> findBySyncUpdatedAtAfter(LocalDateTime dateTime);
    
    /**
     * Busca usuarios por rol y estado activo
     */
    Page<UserEntity> findByRoleAndActive(String role, Boolean active, Pageable pageable);
    
    /**
     * Busca usuarios por texto en nombre o email
     */
    @Query("SELECT u FROM UserEntity u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<UserEntity> findBySearchText(@Param("searchText") String searchText, Pageable pageable);
    
    /**
     * Cuenta usuarios por rol
     */
    long countByRole(String role);
    
    /**
     * Cuenta usuarios activos
     */
    long countByActiveTrue();
    
    /**
     * Verifica si existe un usuario con el ID original
     */
    boolean existsByOriginalId(Long originalId);
    
    /**
     * Elimina usuarios por ID original
     */
    void deleteByOriginalId(Long originalId);
} 