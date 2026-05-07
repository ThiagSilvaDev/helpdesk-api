package com.thiagsilvadev.helpdesk.repository;

import com.thiagsilvadev.helpdesk.entity.user.User;
import com.thiagsilvadev.helpdesk.entity.user.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    List<User> findByRoleInAndActiveTrue(List<Roles> roles);
}
