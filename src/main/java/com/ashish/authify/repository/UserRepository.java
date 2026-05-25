package com.ashish.authify.repository;

import com.ashish.authify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Custom query method to find users by email during Login and OTP Verification
    Optional<User> findByEmail(String email);

    // Custom query method to check if a username or email is already taken during Registration
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}