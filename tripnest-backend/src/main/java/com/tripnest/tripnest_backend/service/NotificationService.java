package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.dto.NotificationDTO;
import com.tripnest.tripnest_backend.entity.Notification;
import com.tripnest.tripnest_backend.entity.NotificationType;
import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Transactional
    public Notification createNotification(User recipient, String message, NotificationType type, Trip trip) {
        return createNotification(recipient, message, type, trip, null, true);
    }

    @Transactional
    public Notification createNotification(User recipient, String message, NotificationType type, Trip trip, Long referenceId) {
        return createNotification(recipient, message, type, trip, referenceId, true);
    }

    @Transactional
    public Notification createNotification(User recipient, String message, NotificationType type, Trip trip, Long referenceId, boolean sendEmail) {
        if (recipient == null) {
            log.warn("Cannot create notification: recipient is null");
            return null;
        }

        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setMessage(message);
        notification.setType(type);
        notification.setTrip(trip);
        notification.setReferenceId(referenceId);
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created for user {}: {}", recipient.getEmail(), message);

        if (sendEmail) {
            // Attempt email dispatch gracefully
            try {
                dispatchEmailForNotification(recipient, message, type, trip, saved);
            } catch (Exception e) {
                log.warn("Email sending failed for notification {}: {}", saved.getId(), e.getMessage());
            }
        }

        return saved;
    }

    private void dispatchEmailForNotification(User recipient, String message, NotificationType type, Trip trip, Notification saved) {
        if (type == null) {
            emailService.sendEmail(recipient.getEmail(), "TripNest Notification", message);
            return;
        }

        switch (type) {
            case MEMBER_ADDED:
                if (message != null && message.contains(" invited you to join the trip")) {
                    log.info("Skipping generic member added email for invitation notification.");
                    break;
                }
                emailService.sendMemberAddedEmail(recipient, trip);
                break;
            case JOIN_REQUEST:
                String requesterName = "A traveler";
                String requesterEmail = null;
                if (message != null && message.contains(" requested to join your trip")) {
                    String prefix = message.substring(0, message.indexOf(" requested to join your trip")).trim();
                    if (prefix.contains("(") && prefix.contains(")")) {
                        requesterName = prefix.substring(0, prefix.indexOf("(")).trim();
                        requesterEmail = prefix.substring(prefix.indexOf("(") + 1, prefix.indexOf(")")).trim();
                    } else {
                        requesterName = prefix;
                    }
                }
                emailService.sendJoinRequestEmail(recipient, requesterName, requesterEmail, trip);
                break;
            case JOIN_REQUEST_APPROVED:
                if (message != null && message.contains(" accepted your invitation to join ")) {
                    log.info("Skipping generic approved email for invitation acceptance.");
                    break;
                }
                emailService.sendJoinRequestApprovedEmail(recipient, trip);
                break;
            case JOIN_REQUEST_REJECTED:
                if (message != null && message.contains(" rejected your invitation to join ")) {
                    log.info("Skipping generic rejected email for invitation rejection.");
                    break;
                }
                emailService.sendJoinRequestRejectedEmail(recipient, trip);
                break;
            case TRIP_REMINDER:
                emailService.sendReminderEmail(recipient, "Upcoming Trip Reminder", message, trip);
                break;
            case ACTIVITY_REMINDER:
                emailService.sendReminderEmail(recipient, "Upcoming Activity Reminder", message, trip);
                break;
            default:
                String subject = "TripNest: " + getSubjectForType(type);
                emailService.sendEmail(recipient.getEmail(), subject, message);
                break;
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(User user) {
        if (user == null) {
            throw new RuntimeException("User must not be null");
        }

        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationDTO markAsRead(Long notificationId, User user) {
        if (user == null) {
            throw new RuntimeException("User must not be null");
        }

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        if (notification.getUser() == null || !notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You cannot modify another user's notification");
        }

        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        return NotificationDTO.fromEntity(updated);
    }

    @Transactional
    public void markAllAsRead(User user) {
        if (user == null) {
            throw new RuntimeException("User must not be null");
        }
        List<Notification> userNotifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        boolean changed = false;
        for (Notification notification : userNotifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
                changed = true;
            }
        }
        if (changed) {
            notificationRepository.saveAll(userNotifications);
        }
        log.info("All notifications marked as read for user {}", user.getEmail());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        if (user == null) {
            return 0;
        }
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public void deleteNotification(Long notificationId, User user) {
        if (user == null) {
            throw new RuntimeException("User must not be null");
        }

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        if (notification.getUser() == null || !notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You cannot delete another user's notification");
        }

        notificationRepository.delete(notification);
        log.info("Notification {} deleted by user {}", notificationId, user.getEmail());
    }

    private String getSubjectForType(NotificationType type) {
        if (type == null) return "New Notification";
        switch (type) {
            case MEMBER_ADDED:
                return "You've been added to a trip!";
            case JOIN_REQUEST:
                return "New trip join request";
            case JOIN_REQUEST_APPROVED:
                return "Your join request has been approved!";
            case JOIN_REQUEST_REJECTED:
                return "Update on your join request";
            case TRIP_REMINDER:
                return "Upcoming Trip Reminder";
            case ACTIVITY_REMINDER:
                return "Upcoming Activity Reminder";
            case BUDGET_ALERT:
                return "Trip Budget Alert";
            case TRAVEL_UPDATE:
                return "Trip Details Update";
            default:
                return "New Notification";
        }
    }
}
