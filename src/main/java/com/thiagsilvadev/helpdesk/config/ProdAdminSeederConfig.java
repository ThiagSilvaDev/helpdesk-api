package com.thiagsilvadev.helpdesk.config;

import com.thiagsilvadev.helpdesk.entity.Roles;
import com.thiagsilvadev.helpdesk.entity.User;
import com.thiagsilvadev.helpdesk.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("prod")
public class ProdAdminSeederConfig {

    private static final Logger log = LoggerFactory.getLogger(ProdAdminSeederConfig.class);

    private record SeedUserSpec(String name, String email, String password, Roles role) {
    }

    @Bean
    CommandLineRunner prodAdminSeeder(UserRepository userRepository,
                                      PasswordEncoder passwordEncoder,
                                      @Value("${app.setup.admin.name}") String adminName,
                                      @Value("${app.setup.admin.email}") String adminEmail,
                                      @Value("${app.setup.admin.password}") String adminPassword) {
        return args -> {
            SeedUserSpec adminSpec = new SeedUserSpec(adminName, adminEmail, adminPassword, Roles.ROLE_ADMIN);
            seedIfAbsent(userRepository, passwordEncoder, adminSpec);
        };
    }

    private void seedIfAbsent(UserRepository userRepository, PasswordEncoder passwordEncoder, SeedUserSpec spec) {
        if (userRepository.existsByEmail(spec.email())) {
            log.info("Production Admin user '{}' already exists. Skipping seed.", spec.email());
            return;
        }

        log.info("Seeding initial Production Admin user: {}", spec.email());
        User user = new User(
                spec.name(),
                spec.email(),
                passwordEncoder.encode(spec.password()),
                spec.role()
        );
        userRepository.save(user);
    }
}