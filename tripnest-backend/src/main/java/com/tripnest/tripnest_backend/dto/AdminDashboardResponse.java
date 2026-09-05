package com.tripnest.tripnest_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    @Builder.Default
    private UserAnalytics userAnalytics = new UserAnalytics();

    @Builder.Default
    private TripAnalytics tripAnalytics = new TripAnalytics();

    @Builder.Default
    private List<DestinationAnalytics> destinationAnalytics = new ArrayList<>();

    @Builder.Default
    private PlatformStats platformStats = new PlatformStats();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAnalytics {
        private Long totalUsers = 0L;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TripAnalytics {
        private Long totalTrips = 0L;
        private Long activeTrips = 0L;
        private Long completedTrips = 0L;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DestinationAnalytics {
        private String destination;
        private Long tripCount = 0L;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlatformStats {
        private Long totalExpenses = 0L;
        private Long totalNotifications = 0L;
        private BigDecimal totalExpenseAmount = BigDecimal.ZERO;
    }
}
