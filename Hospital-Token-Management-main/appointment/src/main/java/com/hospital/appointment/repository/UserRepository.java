package com.hospital.appointment.repository;

import com.hospital.appointment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for User database operations.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     * 
     * @param email user email
     * @return Optional containing the User if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email already exists.
     * 
     * @param email user email
     * @return true if a user exists with this email, false otherwise
     */
    boolean existsByEmail(String email);
}
