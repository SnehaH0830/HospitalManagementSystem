package com.hospital.appointment.repository;

import com.hospital.appointment.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for Role database operations.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find a security role by its name.
     * 
     * @param name role name (e.g. ROLE_PATIENT)
     * @return Optional containing the Role if found
     */
    Optional<Role> findByName(String name);
}
