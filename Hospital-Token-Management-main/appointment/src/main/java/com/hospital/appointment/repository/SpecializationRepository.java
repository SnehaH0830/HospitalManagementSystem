package com.hospital.appointment.repository;

import com.hospital.appointment.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for Specialization database operations.
 */
public interface SpecializationRepository extends JpaRepository<Specialization, Long> {

    /**
     * Find specialization by name.
     * 
     * @param name specialization name
     * @return Optional containing the specialization if found
     */
    Optional<Specialization> findByName(String name);
}
