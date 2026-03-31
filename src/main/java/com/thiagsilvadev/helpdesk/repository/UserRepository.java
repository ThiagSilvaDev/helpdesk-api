package com.thiagsilvadev.helpdesk.repository;

import com.thiagsilvadev.helpdesk.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByIdAndEmail(Long id, String email);

    User findByEmail(String email);
}
