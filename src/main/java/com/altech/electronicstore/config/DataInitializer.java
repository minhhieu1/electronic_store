package com.altech.electronicstore.config;

import com.altech.electronicstore.entity.Basket;
import com.altech.electronicstore.entity.Role;
import com.altech.electronicstore.entity.User;
import com.altech.electronicstore.repository.BasketRepository;
import com.altech.electronicstore.repository.RoleRepository;
import com.altech.electronicstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BasketRepository basketRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
    }

    private void initializeUsers() {
        // Check if users already exist
        if (userRepository.count() > 0) {
            log.info("Users already exist, skipping user initialization");
            return;
        }

        log.info("Initializing users...");

        // Get roles
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("CUSTOMER role not found"));

        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setEmail("admin@electronics-store.com");
        admin.setCreatedAt(LocalDateTime.now());
        admin.setRoles(Set.of(adminRole));
        userRepository.save(admin);
        log.info("Created admin user: {}", admin.getUsername());

        // Create customer1
        User customer1 = new User();
        customer1.setUsername("customer1");
        customer1.setPassword(passwordEncoder.encode("password"));
        customer1.setEmail("customer1@example.com");
        customer1.setCreatedAt(LocalDateTime.now());
        customer1.setRoles(Set.of(customerRole));
        userRepository.save(customer1);
        log.info("Created customer user: {}", customer1.getUsername());

        // Create customer2
        User customer2 = new User();
        customer2.setUsername("customer2");
        customer2.setPassword(passwordEncoder.encode("password"));
        customer2.setEmail("customer2@example.com");
        customer2.setCreatedAt(LocalDateTime.now());
        customer2.setRoles(Set.of(customerRole));
        userRepository.save(customer2);
        log.info("Created customer user: {}", customer2.getUsername());

        // Create baskets for customers
        createBasketForUser(customer1);
        createBasketForUser(customer2);

        log.info("User initialization completed successfully");
    }

    private void createBasketForUser(User user) {
        Basket basket = new Basket();
        basket.setUser(user);
        basketRepository.save(basket);
        log.info("Created basket for user: {}", user.getUsername());
    }
}
