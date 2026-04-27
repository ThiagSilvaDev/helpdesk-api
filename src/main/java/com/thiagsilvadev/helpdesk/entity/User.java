package com.thiagsilvadev.helpdesk.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "users")
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Roles role;

    @Column(nullable = false)
    private boolean active = true;

    public User() {
    }

    public User(String name, String email, String password, Roles role) {
        this.name = requireText(name, "Name");
        this.email = requireText(email, "Email");
        this.password = requireText(password, "Password");
        this.role = Objects.requireNonNull(role, "Role");
    }

    public void rename(String name) {
        this.name = requireText(name, "Name");
    }

    public void changeRole(Roles role) {
        this.role = Objects.requireNonNull(role, "Role must not be null");
    }

    public void changePassword(String password) {
        this.password = requireText(password, "Password");
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }

        return value;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Roles getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }
}
