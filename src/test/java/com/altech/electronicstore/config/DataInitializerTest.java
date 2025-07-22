package com.altech.electronicstore.config;

import com.altech.electronicstore.entity.Basket;
import com.altech.electronicstore.entity.Role;
import com.altech.electronicstore.entity.User;
import com.altech.electronicstore.repository.BasketRepository;
import com.altech.electronicstore.repository.RoleRepository;
import com.altech.electronicstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BasketRepository basketRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    private Role adminRole;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ADMIN");

        customerRole = new Role();
        customerRole.setId(2L);
        customerRole.setName("CUSTOMER");
    }

    @Test
    void run_WhenUsersAlreadyExist_ShouldSkipInitialization() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(5L);

        // When
        dataInitializer.run();

        // Then
        verify(userRepository).count();
        verify(roleRepository, never()).findByName(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(basketRepository, never()).save(any(Basket.class));
    }

    @Test
    void run_WhenNoUsersExist_ShouldInitializeUsersAndBaskets() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(basketRepository.save(any(Basket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        dataInitializer.run();

        // Then
        verify(userRepository).count();
        verify(roleRepository).findByName("ADMIN");
        verify(roleRepository).findByName("CUSTOMER");
        verify(passwordEncoder, times(3)).encode("password");
        verify(userRepository, times(3)).save(any(User.class));
        verify(basketRepository, times(2)).save(any(Basket.class)); // Only customers get baskets
    }

    @Test
    void run_WhenAdminRoleNotFound_ShouldThrowException() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> dataInitializer.run());
        assertEquals("ADMIN role not found", exception.getMessage());
        
        verify(userRepository).count();
        verify(roleRepository).findByName("ADMIN");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void run_WhenCustomerRoleNotFound_ShouldThrowException() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> dataInitializer.run());
        assertEquals("CUSTOMER role not found", exception.getMessage());
        
        verify(userRepository).count();
        verify(roleRepository).findByName("ADMIN");
        verify(roleRepository).findByName("CUSTOMER");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void run_WhenInitializingUsers_ShouldCreateCorrectUserProperties() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(basketRepository.save(any(Basket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Capture user saves
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simulate database ID assignment
            return user;
        });

        // When
        dataInitializer.run();

        // Then
        verify(userRepository, times(3)).save(argThat(user -> {
            assertNotNull(user.getUsername());
            assertEquals("encodedPassword", user.getPassword());
            assertNotNull(user.getEmail());
            assertNotNull(user.getCreatedAt());
            assertNotNull(user.getRoles());
            assertFalse(user.getRoles().isEmpty());
            return true;
        }));
    }

    @Test
    void run_WhenInitializingUsers_ShouldCreateAdminWithCorrectRole() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(basketRepository.save(any(Basket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Capture the admin user save
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if ("admin".equals(user.getUsername())) {
                assertTrue(user.getRoles().contains(adminRole));
                assertEquals("admin@electronics-store.com", user.getEmail());
            }
            return user;
        });

        // When
        dataInitializer.run();

        // Then
        verify(userRepository, times(3)).save(any(User.class));
    }

    @Test
    void run_WhenInitializingUsers_ShouldCreateCustomersWithCorrectRole() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(basketRepository.save(any(Basket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Capture customer user saves
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            if (user.getUsername().startsWith("customer")) {
                assertTrue(user.getRoles().contains(customerRole));
                assertTrue(user.getEmail().contains("@example.com"));
            }
            return user;
        });

        // When
        dataInitializer.run();

        // Then
        verify(userRepository, times(3)).save(any(User.class));
        verify(basketRepository, times(2)).save(argThat(basket -> {
            assertNotNull(basket.getUser());
            return true;
        }));
    }

    @Test
    void run_WhenPasswordEncoderFails_ShouldPropagateException() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("password")).thenThrow(new RuntimeException("Encoding failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> dataInitializer.run());
        
        verify(userRepository).count();
        verify(passwordEncoder).encode("password");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void run_WhenUserRepositoryFails_ShouldPropagateException() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> dataInitializer.run());
        
        verify(userRepository).count();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void run_WhenBasketRepositoryFails_ShouldPropagateException() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(basketRepository.save(any(Basket.class))).thenThrow(new RuntimeException("Basket creation failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> dataInitializer.run());
        
        verify(userRepository, times(3)).save(any(User.class));
        verify(basketRepository).save(any(Basket.class));
    }
}
