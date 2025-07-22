package com.altech.electronicstore.repository;

import com.altech.electronicstore.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    Optional<Permission> findByName(String name);
    
    List<Permission> findByResource(String resource);
    
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.name = :roleName")
    Set<Permission> findPermissionsByRoleName(@Param("roleName") String roleName);

}
