package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.dto.AdminDashboardResponse;
import com.tripnest.tripnest_backend.dto.TravelerDashboardResponse;
import com.tripnest.tripnest_backend.entity.*;
import com.tripnest.tripnest_backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private TravelPreferenceRepository travelPreferenceRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private User testUser;
    private Destination goa;
    private Destination paris;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("traveler@example.com");
        testUser.setName("Traveler One");

        goa = new Destination(1, "Goa");
        paris = new Destination(2, "Paris");
    }

    @Test
    void testGetTravelerDashboard_EmptyTrips() {
        when(tripRepository.findMyTrips(testUser)).thenReturn(new ArrayList<>());
        when(travelPreferenceRepository.findByUserId(1)).thenReturn(Optional.empty());
        when(tripRepository.findMostVisitedDestinationsByUser(testUser)).thenReturn(new ArrayList<>());

        TravelerDashboardResponse response = dashboardService.getTravelerDashboard(testUser);

        assertNotNull(response);
        assertTrue(response.getUpcomingTrips().isEmpty());
        assertEquals(BigDecimal.ZERO, response.getBudgetOverview().getTotalBudget());
        assertEquals(BigDecimal.ZERO, response.getBudgetOverview().getTotalSpent());
        assertTrue(response.getExpenseSummary().isEmpty());
        assertTrue(response.getFavoriteDestinations().isEmpty());
        assertTrue(response.getMostVisitedDestinations().isEmpty());
        assertEquals(0L, response.getTravelStats().getTotalTrips());
        assertEquals(0L, response.getTravelStats().getTotalDestinations());
        assertEquals(BigDecimal.ZERO, response.getTravelStats().getTotalSpent());
    }

    @Test
    void testGetTravelerDashboard_WithTripsAndExpenses() {
        LocalDate today = LocalDate.now();

        Trip pastTrip = new Trip();
        pastTrip.setId(10);
        pastTrip.setUser(testUser);
        pastTrip.setDestination(goa);
        pastTrip.setStartDate(today.minusDays(20));
        pastTrip.setEndDate(today.minusDays(15));
        pastTrip.setBudget(20000.0);
        pastTrip.setStatus("COMPLETED");

        Trip upcomingTrip1 = new Trip();
        upcomingTrip1.setId(11);
        upcomingTrip1.setUser(testUser);
        upcomingTrip1.setDestination(paris);
        upcomingTrip1.setStartDate(today.plusDays(10));
        upcomingTrip1.setEndDate(today.plusDays(15));
        upcomingTrip1.setBudget(50000.0);
        upcomingTrip1.setStatus("PLANNED");

        Trip upcomingTrip2 = new Trip();
        upcomingTrip2.setId(12);
        upcomingTrip2.setUser(testUser);
        upcomingTrip2.setDestination(goa);
        upcomingTrip2.setStartDate(today.plusDays(3));
        upcomingTrip2.setEndDate(today.plusDays(7));
        upcomingTrip2.setBudget(30000.0);
        upcomingTrip2.setStatus("PLANNED");

        when(tripRepository.findMyTrips(testUser)).thenReturn(List.of(pastTrip, upcomingTrip1, upcomingTrip2));

        // Expenses
        when(expenseRepository.getTotalExpensesByTripIds(any())).thenReturn(BigDecimal.valueOf(15000));
        List<Object[]> categorySummary = List.of(
                new Object[]{"FOOD", BigDecimal.valueOf(5000)},
                new Object[]{"ACCOMMODATION", BigDecimal.valueOf(10000)}
        );
        when(expenseRepository.getCategorySummaryByTripIds(any())).thenReturn(categorySummary);

        // Budgets
        Budget budget1 = new Budget();
        budget1.setTripId(11L);
        budget1.setTotalBudget(BigDecimal.valueOf(55000));
        when(budgetRepository.findByTripIdIn(any())).thenReturn(List.of(budget1));

        // Travel Preference
        TravelPreference pref = new TravelPreference();
        pref.setId(1);
        pref.setUser(testUser);
        pref.setFavouriteDestination(paris);
        when(travelPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(pref));

        // Most Visited
        List<Object[]> visited = List.of(
                new Object[]{"Goa", 2L},
                new Object[]{"Paris", 1L}
        );
        when(tripRepository.findMostVisitedDestinationsByUser(testUser)).thenReturn(visited);

        TravelerDashboardResponse response = dashboardService.getTravelerDashboard(testUser);

        assertNotNull(response);
        // Upcoming trips: only future trips, sorted soonest first (upcomingTrip2 is +3 days, upcomingTrip1 is +10 days)
        assertEquals(2, response.getUpcomingTrips().size());
        assertEquals(12, response.getUpcomingTrips().get(0).getId());
        assertEquals(11, response.getUpcomingTrips().get(1).getId());

        // Budget overview: 20000 (trip10 fallback) + 55000 (budget1) + 30000 (trip12 fallback) = 105000
        assertEquals(BigDecimal.valueOf(105000.0), response.getBudgetOverview().getTotalBudget());
        assertEquals(BigDecimal.valueOf(15000), response.getBudgetOverview().getTotalSpent());

        // Expense summary
        assertEquals(2, response.getExpenseSummary().size());
        assertEquals("FOOD", response.getExpenseSummary().get(0).getCategory());

        // Favorite & Most Visited
        assertEquals(1, response.getFavoriteDestinations().size());
        assertEquals("Paris", response.getFavoriteDestinations().get(0).getName());
        assertEquals(2, response.getMostVisitedDestinations().size());
        assertEquals("Goa", response.getMostVisitedDestinations().get(0).getDestination());
        assertEquals(2L, response.getMostVisitedDestinations().get(0).getTripCount());

        // Travel Stats: 3 total trips, 2 distinct destinations (Goa, Paris), 15000 spent
        assertEquals(3L, response.getTravelStats().getTotalTrips());
        assertEquals(2L, response.getTravelStats().getTotalDestinations());
        assertEquals(BigDecimal.valueOf(15000), response.getTravelStats().getTotalSpent());
    }

    @Test
    void testGetAdminDashboard() {
        when(userRepository.count()).thenReturn(45L);
        when(tripRepository.count()).thenReturn(120L);
        when(tripRepository.countActiveTrips()).thenReturn(15L);
        when(tripRepository.countCompletedTrips()).thenReturn(80L);

        List<Object[]> popular = List.of(
                new Object[]{"Goa", 32L},
                new Object[]{"Paris", 28L}
        );
        when(tripRepository.findPopularDestinationsAcrossPlatform()).thenReturn(popular);
        when(expenseRepository.count()).thenReturn(250L);
        when(notificationRepository.count()).thenReturn(95L);
        when(expenseRepository.getTotalPlatformExpenseAmount()).thenReturn(BigDecimal.valueOf(450000));

        AdminDashboardResponse response = dashboardService.getAdminDashboard();

        assertNotNull(response);
        assertEquals(45L, response.getUserAnalytics().getTotalUsers());
        assertEquals(120L, response.getTripAnalytics().getTotalTrips());
        assertEquals(15L, response.getTripAnalytics().getActiveTrips());
        assertEquals(80L, response.getTripAnalytics().getCompletedTrips());
        assertEquals(2, response.getDestinationAnalytics().size());
        assertEquals("Goa", response.getDestinationAnalytics().get(0).getDestination());
        assertEquals(32L, response.getDestinationAnalytics().get(0).getTripCount());
        assertEquals(250L, response.getPlatformStats().getTotalExpenses());
        assertEquals(95L, response.getPlatformStats().getTotalNotifications());
        assertEquals(BigDecimal.valueOf(450000), response.getPlatformStats().getTotalExpenseAmount());
    }
}
