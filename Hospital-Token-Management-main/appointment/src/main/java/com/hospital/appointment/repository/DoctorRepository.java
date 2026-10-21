package com.hospital.appointment.repository;

import com.hospital.appointment.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Doctor database operations.
 */
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /**
     * Find a doctor by the email of their linked User credential.
     * 
     * @param email doctor user email
     * @return Optional containing the Doctor profile if found
     */
    Optional<Doctor> findByUserEmail(String email);

    /**
     * Find all doctors belonging to a specific specialization.
     * 
     * @param specializationId specialization id
     * @return list of doctors
     */
    List<Doctor> findBySpecializationId(Long specializationId);

    /**
     * Search doctors by first or last name case-insensitively.
     * 
     * @param firstName search text for first name
     * @param lastName search text for last name
     * @return list of matching doctors
     */
    List<Doctor> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
}
