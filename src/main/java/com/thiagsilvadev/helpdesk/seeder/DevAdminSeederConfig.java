package com.thiagsilvadev.helpdesk.seeder;

import com.thiagsilvadev.helpdesk.entity.notification.Notification;
import com.thiagsilvadev.helpdesk.entity.notification.NotificationType;
import com.thiagsilvadev.helpdesk.entity.user.Roles;
import com.thiagsilvadev.helpdesk.entity.ticket.Ticket;
import com.thiagsilvadev.helpdesk.entity.ticket.TicketComment;
import com.thiagsilvadev.helpdesk.entity.ticket.TicketPriority;
import com.thiagsilvadev.helpdesk.entity.user.User;
import com.thiagsilvadev.helpdesk.repository.NotificationRepository;
import com.thiagsilvadev.helpdesk.repository.TicketCommentRepository;
import com.thiagsilvadev.helpdesk.repository.TicketRepository;
import com.thiagsilvadev.helpdesk.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
@Profile("dev")
public class DevAdminSeederConfig {

    private static final Logger log = LoggerFactory.getLogger(DevAdminSeederConfig.class);

    private record SeedUserSpec(String name, String email, String password, Roles role, boolean active) {
    }

    private record SeedTicketSpec(
            String title,
            String description,
            String clientEmail,
            TicketPriority priority,
            String technicianEmail,
            TicketFinalState finalState
    ) {
    }

    private record SeedCommentSpec(String ticketTitle, String authorEmail, String content) {
    }

    private enum TicketFinalState {
        KEEP_OPEN,
        CLOSE,
        CANCEL
    }

    @Bean
    CommandLineRunner devAdminSeeder(UserRepository userRepository,
                                     TicketRepository ticketRepository,
                                     TicketCommentRepository ticketCommentRepository,
                                     NotificationRepository notificationRepository,
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
            Map<String, User> usersByEmail = seedUsers(userRepository, passwordEncoder, adminName, adminEmail, adminPassword,
                    userName, userEmail, userPassword, technicianName, technicianEmail, technicianPassword);

            Map<String, Ticket> ticketsByTitle = seedTickets(ticketRepository, usersByEmail, technicianEmail);
            seedCommentsAndNotifications(ticketCommentRepository, notificationRepository, ticketsByTitle, usersByEmail);
        };
    }

    private Map<String, User> seedUsers(UserRepository userRepository,
                                        PasswordEncoder passwordEncoder,
                                        String adminName,
                                        String adminEmail,
                                        String adminPassword,
                                        String userName,
                                        String userEmail,
                                        String userPassword,
                                        String technicianName,
                                        String technicianEmail,
                                        String technicianPassword) {
        List<SeedUserSpec> usersToSeed = List.of(
                new SeedUserSpec(adminName, adminEmail, adminPassword, Roles.ROLE_ADMIN, true),
                new SeedUserSpec(userName, userEmail, userPassword, Roles.ROLE_USER, true),
                new SeedUserSpec(technicianName, technicianEmail, technicianPassword, Roles.ROLE_TECHNICIAN, true),
                new SeedUserSpec("Alicia Customer", "alicia.customer@helpdesk.local", "User@123456", Roles.ROLE_USER, true),
                new SeedUserSpec("Bruno Finance", "bruno.finance@helpdesk.local", "User@123456", Roles.ROLE_USER, true),
                new SeedUserSpec("Carol VIP", "carol.vip@helpdesk.local", "User@123456", Roles.ROLE_USER, true),
                new SeedUserSpec("Diego Field Tech", "diego.tech@helpdesk.local", "Tech@123456", Roles.ROLE_TECHNICIAN, true),
                new SeedUserSpec("Erika Former Client", "erika.former@helpdesk.local", "User@123456", Roles.ROLE_USER, false)
        );

        Map<String, User> usersByEmail = new HashMap<>();
        for (SeedUserSpec spec : usersToSeed) {
            User user = userRepository.findByEmail(spec.email())
                    .orElseGet(() -> {
                        log.info("Seeding development user {}", spec.email());
                        User newUser = new User(
                                spec.name(),
                                spec.email(),
                                passwordEncoder.encode(spec.password()),
                                spec.role()
                        );
                        if (!spec.active()) {
                            newUser.deactivate();
                        }
                        return userRepository.save(newUser);
                    });
            usersByEmail.put(user.getEmail(), user);
        }

        return usersByEmail;
    }

    private Map<String, Ticket> seedTickets(TicketRepository ticketRepository,
                                            Map<String, User> usersByEmail,
                                            String defaultTechnicianEmail) {
        List<Ticket> existingTickets = ticketRepository.findAll();
        Set<String> existingTitles = existingTickets.stream()
                .map(Ticket::getTitle)
                .collect(Collectors.toSet());
        Map<String, Ticket> ticketsByTitle = existingTickets.stream()
                .collect(Collectors.toMap(Ticket::getTitle, ticket -> ticket, (first, ignored) -> first));

        List<SeedTicketSpec> ticketsToSeed = List.of(
                new SeedTicketSpec(
                        "Cannot access customer portal",
                        "Login succeeds but the dashboard keeps redirecting back to the sign-in page.",
                        "carol.vip@helpdesk.local",
                        TicketPriority.URGENT,
                        null,
                        TicketFinalState.KEEP_OPEN
                ),
                new SeedTicketSpec(
                        "Printer offline in finance office",
                        "The network printer is visible but every print job stays queued indefinitely.",
                        "bruno.finance@helpdesk.local",
                        TicketPriority.MEDIUM,
                        defaultTechnicianEmail,
                        TicketFinalState.KEEP_OPEN
                ),
                new SeedTicketSpec(
                        "VPN disconnects every afternoon",
                        "Remote access drops after a few minutes around 3 PM and reconnecting only works temporarily.",
                        "alicia.customer@helpdesk.local",
                        TicketPriority.HIGH,
                        "diego.tech@helpdesk.local",
                        TicketFinalState.KEEP_OPEN
                ),
                new SeedTicketSpec(
                        "Email signatures missing company branding",
                        "New outbound emails are being sent without the standardized footer and legal disclaimer.",
                        userEmail(usersByEmail),
                        TicketPriority.LOW,
                        defaultTechnicianEmail,
                        TicketFinalState.CLOSE
                ),
                new SeedTicketSpec(
                        "Workstation replacement request",
                        "Laptop battery health is below 40 percent and the machine overheats during video calls.",
                        "carol.vip@helpdesk.local",
                        TicketPriority.HIGH,
                        "diego.tech@helpdesk.local",
                        TicketFinalState.CLOSE
                ),
                new SeedTicketSpec(
                        "Duplicate billing notifications",
                        "The requester reported duplicate notices, but finance confirmed the issue was caused by user filters.",
                        "bruno.finance@helpdesk.local",
                        TicketPriority.TRIAGE,
                        null,
                        TicketFinalState.CANCEL
                ),
                new SeedTicketSpec(
                        "Wi-Fi dead spot in meeting room",
                        "Signal drops near the back wall during presentations and screen sharing becomes unstable.",
                        "alicia.customer@helpdesk.local",
                        TicketPriority.MEDIUM,
                        defaultTechnicianEmail,
                        TicketFinalState.CANCEL
                )
        );

        for (SeedTicketSpec spec : ticketsToSeed) {
            if (existingTitles.contains(spec.title())) {
                continue;
            }

            User client = usersByEmail.get(spec.clientEmail());
            Ticket ticket = new Ticket(spec.title(), spec.description(), client, spec.priority());

            if (spec.technicianEmail() != null) {
                ticket.assignTechnician(usersByEmail.get(spec.technicianEmail()));
            }

            if (spec.finalState() == TicketFinalState.CLOSE) {
                ticket.closeTicket();
            } else if (spec.finalState() == TicketFinalState.CANCEL) {
                ticket.cancelTicket();
            }

            Ticket savedTicket = ticketRepository.save(ticket);
            ticketsByTitle.put(savedTicket.getTitle(), savedTicket);
            log.info("Seeding development ticket {}", spec.title());
        }

        return ticketsByTitle;
    }

    private void seedCommentsAndNotifications(TicketCommentRepository ticketCommentRepository,
                                              NotificationRepository notificationRepository,
                                              Map<String, Ticket> ticketsByTitle,
                                              Map<String, User> usersByEmail) {
        SeedCommentSpec commentSpec = new SeedCommentSpec(
                "Printer offline in finance office",
                "bruno.finance@helpdesk.local",
                "I restarted the printer and the queue is still blocked. Finance needs this cleared before invoices go out."
        );

        Ticket ticket = ticketsByTitle.get(commentSpec.ticketTitle());
        User author = usersByEmail.get(commentSpec.authorEmail());

        TicketComment comment = ticketCommentRepository.findByTicketId(ticket.getId(), Pageable.unpaged())
                .stream()
                .filter(existingComment -> existingComment.getContent().equals(commentSpec.content()))
                .findFirst()
                .orElseGet(() -> {
                    log.info("Seeding development comment for ticket {}", commentSpec.ticketTitle());
                    return ticketCommentRepository.save(new TicketComment(ticket, author, commentSpec.content()));
                });

        UUID sourceEventId = UUID.nameUUIDFromBytes(
                ("dev-seed:notification:ticket-comment-created:" + commentSpec.ticketTitle()).getBytes(StandardCharsets.UTF_8)
        );

        if (notificationRepository.existsBySourceEventId(sourceEventId)) {
            return;
        }

        User recipient = ticket.getTechnician() != null ? ticket.getTechnician() : usersByEmail.get("tech@helpdesk.local");
        notificationRepository.save(new Notification(
                recipient,
                NotificationType.TICKET_COMMENT_CREATED,
                "New ticket comment",
                author.getName() + " commented on \"" + ticket.getTitle() + "\".",
                ticket.getId(),
                comment.getId(),
                author.getId(),
                sourceEventId
        ));
        log.info("Seeding development notification for ticket {}", commentSpec.ticketTitle());
    }

    private String userEmail(Map<String, User> usersByEmail) {
        return usersByEmail.values().stream()
                .filter(user -> user.getRole() == Roles.ROLE_USER)
                .map(User::getEmail)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find seeded user for role " + Roles.ROLE_USER));
    }
}
