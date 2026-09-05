package com.tripnest.tripnest_backend.dto;

import com.tripnest.tripnest_backend.entity.Trip;
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
public class TravelerDashboardResponse {

    @Builder.Default
    private List<Trip> upcomingTrips = new ArrayList<>();

    @Builder.Default
    private BudgetOverview budgetOverview = new BudgetOverview();

    @Builder.Default
    private List<ExpenseCategorySummary> expenseSummary = new ArrayList<>();

    @Builder.Default
    private List<FavoriteDestinationDTO> favoriteDestinations = new ArrayList<>();

    @Builder.Default
    private List<DestinationVisitSummary> mostVisitedDestinations = new ArrayList<>();

    @Builder.Default
    private TravelStats travelStats = new TravelStats();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetOverview {
        private BigDecimal totalBudget = BigDecimal.ZERO;
        private BigDecimal totalSpent = BigDecimal.ZERO;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseCategorySummary {
        private String category;
        private BigDecimal amount = BigDecimal.ZERO;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FavoriteDestinationDTO {
        private Integer id;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DestinationVisitSummary {
        private String destination;
        private Long tripCount = 0L;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TravelStats {
        private Long totalTrips = 0L;
        private Long totalDestinations = 0L;
        private BigDecimal totalSpent = BigDecimal.ZERO;
    }
}
