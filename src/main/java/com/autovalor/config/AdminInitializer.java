package com.autovalor.config;

import com.autovalor.user.Role;
import com.autovalor.user.User;
import com.autovalor.user.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminName;
    private final String adminEmail;
    private final String adminPassword;

    public AdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.name:AutoValor Admin}") String adminName,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            return;
        }

        List<User> admins = userRepository.findAllByRole(Role.ADMIN);
        String normalizedAdminEmail = adminEmail.trim().toLowerCase();

        if (!admins.isEmpty()) {
            ensureThereIsOnlyOneAdmin(admins, normalizedAdminEmail);
            return;
        }

        User admin = new User(
                adminName.trim().isBlank() ? "AutoValor Admin" : adminName.trim(),
                normalizedAdminEmail,
                passwordEncoder.encode(adminPassword),
                Role.ADMIN
        );

        userRepository.save(admin);
    }

    private void ensureThereIsOnlyOneAdmin(List<User> admins, String configuredAdminEmail) {
        User mainAdmin = admins.stream()
                .filter(admin -> admin.getEmail().equalsIgnoreCase(configuredAdminEmail))
                .findFirst()
                .orElse(admins.getFirst());

        for (User admin : admins) {
            if (!admin.getId().equals(mainAdmin.getId())) {
                admin.setRole(Role.USER);
                userRepository.save(admin);
            }
        }
    }
}
