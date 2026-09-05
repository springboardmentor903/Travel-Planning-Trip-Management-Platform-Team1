package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.dto.AuthResponse;
import com.tripnest.tripnest_backend.dto.LoginRequest;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.RoleRepository;
import com.tripnest.tripnest_backend.repository.UserRepository;
import com.tripnest.tripnest_backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceLoginEmailTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPasswordHash("hashed_password");
    }

    private LoginRequest createRequest(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    @Test
    void testLoginUser_SuccessfulLogin_TriggersLoginEmail() {
        LoginRequest request = createRequest("john@example.com", "password123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
        when(jwtUtil.generateToken("john@example.com")).thenReturn("mocked-jwt-token");

        AuthResponse response = userService.loginUser(request);

        assertNotNull(response);
        assertEquals("Login successful", response.getMessage());
        assertEquals("mocked-jwt-token", response.getToken());

        verify(emailService, times(1)).sendLoginNotificationEmail(eq(user), any(LocalDateTime.class));
    }

    @Test
    void testLoginUser_WrongPassword_DoesNotTriggerLoginEmail() {
        LoginRequest request = createRequest("john@example.com", "wrong_password");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_password", "hashed_password")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userService.loginUser(request)
        );

        assertEquals("Invalid email or password", ex.getMessage());
        verify(emailService, never()).sendLoginNotificationEmail(any(), any());
    }

    @Test
    void testLoginUser_UserNotFound_DoesNotTriggerLoginEmail() {
        LoginRequest request = createRequest("unknown@example.com", "password123");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userService.loginUser(request)
        );

        assertEquals("Invalid email or password", ex.getMessage());
        verify(emailService, never()).sendLoginNotificationEmail(any(), any());
    }

    @Test
    void testLoginUser_EmailFailureDoesNotBreakLogin() {
        LoginRequest request = createRequest("john@example.com", "password123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
        when(jwtUtil.generateToken("john@example.com")).thenReturn("mocked-jwt-token");

        doThrow(new RuntimeException("Email service failure")).when(emailService)
                .sendLoginNotificationEmail(any(), any());

        AuthResponse response = assertDoesNotThrow(() ->
                userService.loginUser(request)
        );

        assertNotNull(response);
        assertEquals("Login successful", response.getMessage());
        assertEquals("mocked-jwt-token", response.getToken());
    }
}
