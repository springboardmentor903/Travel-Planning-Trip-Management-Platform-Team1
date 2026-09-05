package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.dto.NotificationDTO;
import com.tripnest.tripnest_backend.entity.Destination;
import com.tripnest.tripnest_backend.entity.Notification;
import com.tripnest.tripnest_backend.entity.NotificationType;
import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    private User user1;
    private User user2;
    private Trip trip;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1);
        user1.setName("Alice");
        user1.setEmail("alice@example.com");

        user2 = new User();
        user2.setId(2);
        user2.setName("Bob");
        user2.setEmail("bob@example.com");

        trip = new Trip();
        trip.setId(10);
        trip.setUser(user1);
        Destination destination = new Destination();
        destination.setId(100);
        destination.setName("Goa");
        trip.setDestination(destination);
    }

    @Test
    void testCreateNotification_Success() {
        Notification savedNotification = new Notification();
        savedNotification.setId(101L);
        savedNotification.setUser(user1);
        savedNotification.setMessage("You have been added to the trip: Goa");
        savedNotification.setType(NotificationType.MEMBER_ADDED);
        savedNotification.setTrip(trip);
        savedNotification.setRead(false);
        savedNotification.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        Notification result = notificationService.createNotification(
                user1,
                "You have been added to the trip: Goa",
                NotificationType.MEMBER_ADDED,
                trip
        );

        assertNotNull(result);
        assertEquals(101L, result.getId());
        assertEquals("You have been added to the trip: Goa", result.getMessage());
        assertEquals(NotificationType.MEMBER_ADDED, result.getType());
        assertFalse(result.isRead());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(emailService, times(1)).sendMemberAddedEmail(eq(user1), eq(trip));
    }

    @Test
    void testGetUserNotifications_ReturnsOnlyUserNotifications() {
        Notification n1 = new Notification();
        n1.setId(1L);
        n1.setUser(user1);
        n1.setMessage("Message 1");
        n1.setType(NotificationType.MEMBER_ADDED);
        n1.setRead(false);
        n1.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.findByUserOrderByCreatedAtDesc(user1))
                .thenReturn(List.of(n1));

        List<NotificationDTO> result = notificationService.getUserNotifications(user1);

        assertEquals(1, result.size());
        assertEquals("Message 1", result.get(0).getMessage());
        verify(notificationRepository, times(1)).findByUserOrderByCreatedAtDesc(user1);
    }

    @Test
    void testMarkAsRead_Success() {
        Notification n = new Notification();
        n.setId(1L);
        n.setUser(user1);
        n.setMessage("Message 1");
        n.setRead(false);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationDTO result = notificationService.markAsRead(1L, user1);

        assertNotNull(result);
        assertTrue(result.isRead());
        verify(notificationRepository, times(1)).save(n);
    }

    @Test
    void testMarkAsRead_AccessDeniedForOtherUser() {
        Notification n = new Notification();
        n.setId(1L);
        n.setUser(user1); // Belongs to user1
        n.setMessage("Message 1");
        n.setRead(false);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        // User2 attempts to mark user1's notification as read
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                notificationService.markAsRead(1L, user2)
        );

        assertTrue(exception.getMessage().contains("Access denied"));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testMarkAsRead_NotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                notificationService.markAsRead(999L, user1)
        );

        assertTrue(exception.getMessage().contains("not found"));
    }
}
