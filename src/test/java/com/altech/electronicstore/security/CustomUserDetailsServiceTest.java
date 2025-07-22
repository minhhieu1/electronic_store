package com.altech.electronicstore.security;

import com.altech.electronicstore.entity.Role;
import com.altech.electronicstore.entity.User;
import com.altech.electronicstore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_WithValidUser_ShouldReturnUserDetails() {
        // Given
        String username = "testuser";
        String password = "encodedPassword";
        
        Role adminRole = createRole(1L, "ADMIN");
        Role userRole = createRole(2L, "USER");
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        roles.add(userRole);
        
        User user = createUser(1L, username, password, "test@example.com", roles);
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(password, result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        
        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
        assertEquals(2, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_WithSingleRole_ShouldReturnUserDetailsWithOneAuthority() {
        // Given
        String username = "customeruser";
        String password = "customerPassword";
        
        Role customerRole = createRole(3L, "CUSTOMER");
        Set<Role> roles = new HashSet<>();
        roles.add(customerRole);
        
        User user = createUser(2L, username, password, "customer@example.com", roles);
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(password, result.getPassword());
        
        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }

    @Test
    void loadUserByUsername_WithNoRoles_ShouldReturnUserDetailsWithEmptyAuthorities() {
        // Given
        String username = "noroleuser";
        String password = "noRolePassword";
        
        Set<Role> emptyRoles = new HashSet<>();
        User user = createUser(3L, username, password, "norole@example.com", emptyRoles);
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(password, result.getPassword());
        
        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
        assertEquals(0, authorities.size());
    }

    @Test
    void loadUserByUsername_WithNonExistentUser_ShouldThrowUsernameNotFoundException() {
        // Given
        String nonExistentUsername = "nonexistent";
        
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername(nonExistentUsername)
        );
        
        assertEquals("User not found: " + nonExistentUsername, exception.getMessage());
    }

    @Test
    void loadUserByUsername_WithSpecialCharacterUsername_ShouldWork() {
        // Given
        String specialUsername = "user@domain.com";
        String password = "specialPassword";
        
        Role userRole = createRole(1L, "USER");
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        
        User user = createUser(4L, specialUsername, password, "special@example.com", roles);
        
        when(userRepository.findByUsername(specialUsername)).thenReturn(Optional.of(user));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(specialUsername);

        // Then
        assertNotNull(result);
        assertEquals(specialUsername, result.getUsername());
        assertEquals(password, result.getPassword());
        
        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_WithMultipleRolesSamePrefix_ShouldCreateCorrectAuthorities() {
        // Given
        String username = "multiuser";
        String password = "multiPassword";
        
        Role adminRole = createRole(1L, "ADMIN");
        Role adminUserRole = createRole(2L, "ADMIN_USER");
        Role superAdminRole = createRole(3L, "SUPER_ADMIN");
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        roles.add(adminUserRole);
        roles.add(superAdminRole);
        
        User user = createUser(5L, username, password, "multi@example.com", roles);
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(result);
        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
        assertEquals(3, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN_USER")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
    }

    @Test
    void loadUserByUsername_WithEmptyUsername_ShouldThrowUsernameNotFoundException() {
        // Given
        String emptyUsername = "";
        
        when(userRepository.findByUsername(emptyUsername)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, 
                    () -> customUserDetailsService.loadUserByUsername(emptyUsername));
    }

    @Test
    void loadUserByUsername_WithNullRoles_ShouldHandleGracefully() {
        // Given
        String username = "nullrolesuser";
        String password = "nullPassword";
        
        User user = createUser(6L, username, password, "null@example.com", null);
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(NullPointerException.class, 
                    () -> customUserDetailsService.loadUserByUsername(username));
    }

    // Helper methods
    private User createUser(Long id, String username, String password, String email, Set<Role> roles) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setRoles(roles);
        return user;
    }

    private Role createRole(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }
}
