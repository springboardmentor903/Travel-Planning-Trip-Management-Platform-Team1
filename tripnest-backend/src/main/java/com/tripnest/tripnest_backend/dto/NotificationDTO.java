package com.tripnest.tripnest_backend.dto;

import com.tripnest.tripnest_backend.entity.Notification;
import com.tripnest.tripnest_backend.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;
    private String message;
    private NotificationType type;
    private boolean isRead;
    private Integer tripId;
    private String tripDestination;
    private Long referenceId;
    private LocalDateTime createdAt;

    public static NotificationDTO fromEntity(Notification notification) {
        if (notification == null) {
            return null;
        }

        Integer tripId = null;
        String tripDestination = null;

        if (notification.getTrip() != null) {
            tripId = notification.getTrip().getId();
            if (notification.getTrip().getDestination() != null) {
                tripDestination = notification.getTrip().getDestination().getName();
            }
        }

        return NotificationDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.isRead())
                .tripId(tripId)
                .tripDestination(tripDestination)
                .referenceId(notification.getReferenceId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
