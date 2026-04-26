package com.thiagsilvadev.helpdesk.repository;

import com.thiagsilvadev.helpdesk.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<User> findByEmail(String email);
}
