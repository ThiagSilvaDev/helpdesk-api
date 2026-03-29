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

@Configuration
@Profile("dev")
public class DevAdminSeederConfig {

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
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = new User(
                        adminName,
                        adminEmail,
                        passwordEncoder.encode(adminPassword),
                        Roles.ROLE_ADMIN
                );
                userRepository.save(admin);
            }

            if (!userRepository.existsByEmail(userEmail)) {
                User user = new User(
                        userName,
                        userEmail,
                        passwordEncoder.encode(userPassword),
                        Roles.ROLE_USER
                );
                userRepository.save(user);
            }

            if (!userRepository.existsByEmail(technicianEmail)) {
                User technician = new User(
                        technicianName,
                        technicianEmail,
                        passwordEncoder.encode(technicianPassword),
                        Roles.ROLE_TECHNICIAN
                );
                userRepository.save(technician);
            }
        };
    }
}

