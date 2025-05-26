package com.laba.labais.security;

import com.laba.labais.entity.User;
import com.laba.labais.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("encodedPassword")
                .role(User.Role.USER)
                .build();
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(testUser.getEmail());
        assertThat(userDetails.getPassword()).isEqualTo(testUser.getPassword());
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next())
                .isEqualTo(new SimpleGrantedAuthority("ROLE_USER"));
        
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> 
            userDetailsService.loadUserByUsername("nonexistent@example.com")
        );
        
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetailsWithAdminRole_WhenUserIsAdmin() {
        // Arrange
        User adminUser = User.builder()
                .id(2L)
                .email("admin@example.com")
                .username("admin")
                .password("encodedPassword")
                .role(User.Role.ADMIN)
                .build();
                
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(adminUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@example.com");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(adminUser.getEmail());
        assertThat(userDetails.getPassword()).isEqualTo(adminUser.getPassword());
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next())
                .isEqualTo(new SimpleGrantedAuthority("ROLE_ADMIN"));
        
        verify(userRepository).findByEmail("admin@example.com");
    }
}