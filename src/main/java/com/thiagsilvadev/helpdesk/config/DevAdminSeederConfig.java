package com.thiagsilvadev.helpdesk.config;

import com.thiagsilvadev.helpdesk.entity.Roles;
import com.thiagsilvadev.helpdesk.entity.User;
import com.thiagsilvadev.helpdesk.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@Profile("dev")
public class DevAdminSeederConfig {

    private record SeedUserSpec(String name, String email, String password, Roles role) {
    }

    @Bean
    CommandLineRunner devAdminSeeder(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     @Value("${app.dev-admin.name}") String adminName,
                                     @Value("${app.dev-admin.email}") String adminEmail,
                                     @Value("${app.dev-admin.password}") String adminPassword,
                                     @Value("${app.dev-user.name}") String userName,
                                     @Value("${app.dev-user.email}") String userEmail,
                                     @Value("${app.dev-user.password}") String userPassword,
                                     @Value("${app.dev-tech.name}") String technicianName,
                                     @Value("${app.dev-tech.email}") String technicianEmail,
                                     @Value("${app.dev-tech.password}") String technicianPassword) {
        return args -> {
            List<SeedUserSpec> usersToSeed = List.of(
                    new SeedUserSpec(adminName, adminEmail, adminPassword, Roles.ROLE_ADMIN),
                    new SeedUserSpec(userName, userEmail, userPassword, Roles.ROLE_USER),
                    new SeedUserSpec(technicianName, technicianEmail, technicianPassword, Roles.ROLE_TECHNICIAN)
            );

            usersToSeed.forEach(spec -> seedIfAbsent(userRepository, passwordEncoder, spec));
        };
    }

    private void seedIfAbsent(UserRepository userRepository, PasswordEncoder passwordEncoder, SeedUserSpec spec) {
        if (userRepository.existsByEmail(spec.email())) {
            return;
        }

        User user = new User(
                spec.name(),
                spec.email(),
                passwordEncoder.encode(spec.password()),
                spec.role()
        );
        userRepository.save(user);
    }
}
