package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.dto.AdminDashboardResponse;
import com.tripnest.tripnest_backend.dto.TravelerDashboardResponse;
import com.tripnest.tripnest_backend.entity.Budget;
import com.tripnest.tripnest_backend.entity.Destination;
import com.tripnest.tripnest_backend.entity.TravelPreference;
import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.BudgetRepository;
import com.tripnest.tripnest_backend.repository.DestinationRepository;
import com.tripnest.tripnest_backend.repository.ExpenseRepository;
import com.tripnest.tripnest_backend.repository.NotificationRepository;
import com.tripnest.tripnest_backend.repository.TravelPreferenceRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
import com.tripnest.tripnest_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final DestinationRepository destinationRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final NotificationRepository notificationRepository;
    private final TravelPreferenceRepository travelPreferenceRepository;

    // ==========================================
    // TRAVELER DASHBOARD
    // ==========================================
    public TravelerDashboardResponse getTravelerDashboard(User user) {
        if (user == null) {
            return new TravelerDashboardResponse();
        }

        List<Trip> allUserTrips = tripRepository.findMyTrips(user);
        if (allUserTrips == null) {
            allUserTrips = new ArrayList<>();
        }

        LocalDate today = LocalDate.now();

        // 1. Upcoming Trips (future trips, sorted ascending by startDate soonest first)
        List<Trip> upcomingTrips = allUserTrips.stream()
                .filter(t -> t.getStartDate() != null && !t.getStartDate().isBefore(today))
                .sorted(Comparator.comparing(Trip::getStartDate))
                .toList();

        // Collect trip IDs
        List<Long> tripIds = allUserTrips.stream()
                .filter(t -> t.getId() != null)
                .map(t -> t.getId().longValue())
                .toList();

        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;
        List<TravelerDashboardResponse.ExpenseCategorySummary> expenseSummary = new ArrayList<>();

        if (!tripIds.isEmpty()) {
            // Total spent across user's trips
            BigDecimal spent = expenseRepository.getTotalExpensesByTripIds(tripIds);
            if (spent != null) {
                totalSpent = spent;
            }

            // Total budget across user's trips (prefer detailed Budget entity, fallback to Trip.budget)
            List<Budget> budgets = budgetRepository.findByTripIdIn(tripIds);
            Map<Long, Budget> budgetMap = budgets.stream()
                    .filter(b -> b.getTripId() != null)
                    .collect(Collectors.toMap(Budget::getTripId, b -> b, (b1, b2) -> b1));

            for (Trip t : allUserTrips) {
                Long tid = t.getId().longValue();
                if (budgetMap.containsKey(tid) && budgetMap.get(tid).getTotalBudget() != null) {
                    totalBudget = totalBudget.add(budgetMap.get(tid).getTotalBudget());
                } else if (t.getBudget() != null) {
                    totalBudget = totalBudget.add(BigDecimal.valueOf(t.getBudget()));
                }
            }

            // 3. Expense Summary across all user trips
            List<Object[]> categoryData = expenseRepository.getCategorySummaryByTripIds(tripIds);
            if (categoryData != null) {
                expenseSummary = categoryData.stream()
                        .map(row -> new TravelerDashboardResponse.ExpenseCategorySummary(
                                (String) row[0],
                                row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO
                        ))
                        .toList();
            }
        }

        // 4. Favorite Destinations (persistent TravelPreference) & Most-Visited Destinations
        List<TravelerDashboardResponse.FavoriteDestinationDTO> favoriteDestinations = new ArrayList<>();
        Optional<TravelPreference> preferenceOpt = travelPreferenceRepository.findByUserId(user.getId());
        if (preferenceOpt.isPresent() && preferenceOpt.get().getFavouriteDestination() != null) {
            Destination fav = preferenceOpt.get().getFavouriteDestination();
            favoriteDestinations.add(new TravelerDashboardResponse.FavoriteDestinationDTO(
                    fav.getId(),
                    fav.getName()
            ));
        }

        List<TravelerDashboardResponse.DestinationVisitSummary> mostVisitedDestinations = new ArrayList<>();
        List<Object[]> visitedData = tripRepository.findMostVisitedDestinationsByUser(user);
        if (visitedData != null) {
            mostVisitedDestinations = visitedData.stream()
                    .map(row -> new TravelerDashboardResponse.DestinationVisitSummary(
                            (String) row[0],
                            row[1] != null ? ((Number) row[1]).longValue() : 0L
                    ))
                    .toList();
        }

        // 5. Basic Travel Stats
        long totalTrips = allUserTrips.size();
        long totalDestinations = allUserTrips.stream()
                .map(Trip::getDestination)
                .filter(Objects::nonNull)
                .map(Destination::getName)
                .filter(Objects::nonNull)
                .filter(s -> !s.trim().isEmpty())
                .distinct()
                .count();

        return TravelerDashboardResponse.builder()
                .upcomingTrips(upcomingTrips)
                .budgetOverview(new TravelerDashboardResponse.BudgetOverview(totalBudget, totalSpent))
                .expenseSummary(expenseSummary)
                .favoriteDestinations(favoriteDestinations)
                .mostVisitedDestinations(mostVisitedDestinations)
                .travelStats(new TravelerDashboardResponse.TravelStats(totalTrips, totalDestinations, totalSpent))
                .build();
    }

    // ==========================================
    // ADMIN DASHBOARD
    // ==========================================
    public AdminDashboardResponse getAdminDashboard() {
        // 1. User Analytics
        long totalUsers = userRepository.count();

        // 2. Trip Analytics
        long totalTrips = tripRepository.count();
        long activeTrips = tripRepository.countActiveTrips();
        long completedTrips = tripRepository.countCompletedTrips();

        // 3. Destination Analytics
        List<TravelerDashboardResponse.DestinationVisitSummary> destAnalytics = new ArrayList<>();
        List<Object[]> popularData = tripRepository.findPopularDestinationsAcrossPlatform();
        List<AdminDashboardResponse.DestinationAnalytics> destinationAnalytics = new ArrayList<>();
        if (popularData != null) {
            destinationAnalytics = popularData.stream()
                    .map(row -> new AdminDashboardResponse.DestinationAnalytics(
                            (String) row[0],
                            row[1] != null ? ((Number) row[1]).longValue() : 0L
                    ))
                    .toList();
        }

        // 4. Platform Stats
        long totalExpenses = expenseRepository.count();
        long totalNotifications = notificationRepository.count();
        BigDecimal totalExpenseAmount = expenseRepository.getTotalPlatformExpenseAmount();
        if (totalExpenseAmount == null) {
            totalExpenseAmount = BigDecimal.ZERO;
        }

        return AdminDashboardResponse.builder()
                .userAnalytics(new AdminDashboardResponse.UserAnalytics(totalUsers))
                .tripAnalytics(new AdminDashboardResponse.TripAnalytics(totalTrips, activeTrips, completedTrips))
                .destinationAnalytics(destinationAnalytics)
                .platformStats(new AdminDashboardResponse.PlatformStats(totalExpenses, totalNotifications, totalExpenseAmount))
                .build();
    }
}
