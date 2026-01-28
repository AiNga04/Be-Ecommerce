package com.zyna.dev.ecommerce.seeders;

import com.zyna.dev.ecommerce.authentication.repository.AppRoleRepository;
import com.zyna.dev.ecommerce.common.enums.Status;
import com.zyna.dev.ecommerce.users.models.User;
import com.zyna.dev.ecommerce.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Order(4)
public class AdminUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AppRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.existsByEmail("tuhocbackend@gmail.com")) return;

        var adminRole = roleRepository.findByCode("ADMIN")
                .orElseThrow();

        User admin = User.builder()
                .firstName("System")
                .lastName("Admin")
                .email("tuhocbackend@gmail.com")
                .password(passwordEncoder.encode("Admin@123"))
                .roles(Set.of(adminRole))
                .status(Status.ACTIVE)
                .build();

        userRepository.save(admin);
    }
}
