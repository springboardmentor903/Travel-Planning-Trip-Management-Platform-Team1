package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.dto.NotificationDTO;
import com.tripnest.tripnest_backend.entity.NotificationType;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.UserRepository;
import com.tripnest.tripnest_backend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NotificationController notificationController;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1);
        currentUser.setEmail("user@example.com");
        currentUser.setName("Test User");
    }

    @Test
    void testGetMyNotifications_Authenticated() {
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(currentUser));

        NotificationDTO dto = NotificationDTO.builder()
                .id(1L)
                .message("Test message")
                .type(NotificationType.MEMBER_ADDED)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationService.getUserNotifications(currentUser)).thenReturn(List.of(dto));

        ResponseEntity<?> response = notificationController.getMyNotifications(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<?> list = (List<?>) response.getBody();
        assertEquals(1, list.size());
    }

    @Test
    void testMarkAsRead_Success() {
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(currentUser));

        NotificationDTO dto = NotificationDTO.builder()
                .id(1L)
                .message("Test message")
                .isRead(true)
                .build();

        when(notificationService.markAsRead(1L, currentUser)).thenReturn(dto);

        ResponseEntity<?> response = notificationController.markAsRead(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void testMarkAsRead_ForbiddenWhenOtherUser() {
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(currentUser));

        when(notificationService.markAsRead(1L, currentUser))
                .thenThrow(new RuntimeException("Access denied: You cannot modify another user's notification"));

        ResponseEntity<?> response = notificationController.markAsRead(1L, authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).get("message").toString().contains("Access denied"));
    }

    @Test
    void testMarkAsRead_NotFound() {
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(currentUser));

        when(notificationService.markAsRead(99L, currentUser))
                .thenThrow(new RuntimeException("Notification not found with id: 99"));

        ResponseEntity<?> response = notificationController.markAsRead(99L, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
