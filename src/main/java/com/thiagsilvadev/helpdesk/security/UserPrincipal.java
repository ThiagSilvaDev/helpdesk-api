package com.thiagsilvadev.helpdesk.security;

import com.thiagsilvadev.helpdesk.entity.User;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = Objects.requireNonNull(user, "user must not be null");
    }

    public Long getId() {
        return user.getId();
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    @NonNull
    public String getPassword() {
        return Objects.requireNonNull(user.getPassword(), "user password must not be null");
    }

    @Override
    @NonNull
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}
