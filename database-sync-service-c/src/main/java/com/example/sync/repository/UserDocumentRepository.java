package com.example.sync.repository;

import com.example.sync.document.UserDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio MongoDB para operaciones con documentos de usuario
 */
@Repository
public interface UserDocumentRepository extends MongoRepository<UserDocument, String> {
    
    /**
     * Busca un usuario por su ID original
     */
    Optional<UserDocument> findByOriginalId(Long originalId);
    
    /**
     * Busca un usuario por su userId
     */
    Optional<UserDocument> findByUserId(String userId);
    
    /**
     * Busca un usuario por email
     */
    Optional<UserDocument> findByEmail(String email);
    
    /**
     * Busca un usuario por username
     */
    Optional<UserDocument> findByUsername(String username);
    
    /**
     * Busca usuarios por rol
     */
    List<UserDocument> findByRole(String role);
    
    /**
     * Busca usuarios activos
     */
    List<UserDocument> findByActiveTrue();
    
    /**
     * Busca usuarios por departamento
     */
    List<UserDocument> findByDepartment(String department);
    
    /**
     * Busca usuarios por especialización
     */
    List<UserDocument> findBySpecialization(String specialization);
    
    /**
     * Busca usuarios por rol y estado activo con paginación
     */
    Page<UserDocument> findByRoleAndActive(String role, Boolean active, Pageable pageable);
    
    /**
     * Busca usuarios por texto en nombre, email o username
     */
    @Query("{ $or: [ " +
           "{ 'full_name': { $regex: ?0, $options: 'i' } }, " +
           "{ 'email': { $regex: ?0, $options: 'i' } }, " +
           "{ 'username': { $regex: ?0, $options: 'i' } } " +
           "] }")
    Page<UserDocument> findBySearchText(String searchText, Pageable pageable);
    
    /**
     * Cuenta usuarios activos
     */
    long countByActiveTrue();
    
    /**
     * Busca usuarios sincronizados después de una fecha
     */
    List<UserDocument> findBySyncUpdatedAtAfter(LocalDateTime dateTime);
    
    /**
     * Busca usuarios por estado de sincronización
     */
    List<UserDocument> findBySyncStatus(UserDocument.SyncStatus syncStatus);
    
    /**
     * Verifica si existe un usuario con el email dado
     */
    boolean existsByEmail(String email);
    
    /**
     * Verifica si existe un usuario con el username dado
     */
    boolean existsByUsername(String username);
    
    /**
     * Elimina usuarios por originalId
     */
    void deleteByOriginalId(Long originalId);
} 