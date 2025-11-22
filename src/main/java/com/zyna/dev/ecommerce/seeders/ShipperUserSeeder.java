package com.zyna.dev.ecommerce.seeders;

import com.zyna.dev.ecommerce.authentication.repository.AppRoleRepository;
import com.zyna.dev.ecommerce.common.enums.Status;
import com.zyna.dev.ecommerce.users.User;
import com.zyna.dev.ecommerce.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Order(5)
public class ShipperUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AppRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail("shipper@zyna.dev")) return;

        var shipperRole = roleRepository.findByCode("SHIPPER")
                .orElseThrow();

        User shipper = User.builder()
                .firstName("Default")
                .lastName("Shipper")
                .email("shipper@zyna.dev")
                .password(passwordEncoder.encode("Shipper@123"))
                .roles(Set.of(shipperRole))
                .status(Status.ACTIVE)
                .build();

        userRepository.save(shipper);
    }
}
