package com.tripnest.tripnest_backend.service;

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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseBudgetAlertTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TripAccessService tripAccessService;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripMembershipRepository tripMembershipRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private User owner;
    private User member;
    private Trip trip;
    private Budget budget;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1);
        owner.setEmail("owner@example.com");

        member = new User();
        member.setId(2);
        member.setEmail("member@example.com");

        Destination dest = new Destination(10, "Paris");

        trip = new Trip();
        trip.setId(100);
        trip.setUser(owner);
        trip.setDestination(dest);

        budget = new Budget();
        org.springframework.test.util.ReflectionTestUtils.setField(budget, "id", 5L);
        budget.setTripId(100L);
        budget.setTotalBudget(BigDecimal.valueOf(100000)); // 100,000
    }

    @Test
    void testCreateExpense_Triggers80PercentAlert_WhenThresholdReached() {
        Expense expense = new Expense();
        expense.setTripId(100L);
        expense.setBudgetId(5L);
        expense.setPayerId(1L);
        expense.setCategory("FOOD");
        expense.setAmount(BigDecimal.valueOf(80000));
        expense.setExpenseDate(LocalDate.now());

        when(budgetRepository.findByTripId(100L)).thenReturn(Optional.of(budget));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        when(tripRepository.findById(100)).thenReturn(Optional.of(trip));
        when(expenseRepository.getTotalExpenses(100L)).thenReturn(BigDecimal.valueOf(80000)); // 80%

        TripMembership tm = new TripMembership();
        tm.setUser(member);
        when(tripMembershipRepository.findByTripId(100L)).thenReturn(List.of(tm));

        // Not yet alerted for 80%
        when(notificationRepository.existsByTripIdAndTypeAndReferenceId(100, NotificationType.BUDGET_ALERT, 80L))
                .thenReturn(false);

        expenseService.createExpense(expense, owner);

        verify(notificationService, times(1)).createNotification(
                eq(owner), contains("80%"), eq(NotificationType.BUDGET_ALERT), eq(trip), eq(80L));
        verify(notificationService, times(1)).createNotification(
                eq(member), contains("80%"), eq(NotificationType.BUDGET_ALERT), eq(trip), eq(80L));
    }

    @Test
    void testUpdateExpense_Triggers100PercentAlert_WhenThresholdReached() {
        Expense existingExpense = new Expense();
        org.springframework.test.util.ReflectionTestUtils.setField(existingExpense, "id", 1L);
        existingExpense.setTripId(100L);
        existingExpense.setBudgetId(5L);
        existingExpense.setPayerId(1L);
        existingExpense.setCategory("FOOD");
        existingExpense.setAmount(BigDecimal.valueOf(50000));
        existingExpense.setExpenseDate(LocalDate.now());

        Expense updatedExpense = new Expense();
        updatedExpense.setTripId(100L);
        updatedExpense.setBudgetId(5L);
        updatedExpense.setPayerId(1L);
        updatedExpense.setCategory("FOOD");
        updatedExpense.setAmount(BigDecimal.valueOf(100000));
        updatedExpense.setExpenseDate(LocalDate.now());

        when(expenseRepository.findByIdAndTripId(1L, 100L)).thenReturn(Optional.of(existingExpense));
        when(budgetRepository.findByTripId(100L)).thenReturn(Optional.of(budget));
        when(expenseRepository.save(any(Expense.class))).thenReturn(existingExpense);
        when(tripRepository.findById(100)).thenReturn(Optional.of(trip));
        when(expenseRepository.getTotalExpenses(100L)).thenReturn(BigDecimal.valueOf(100000)); // 100%

        when(tripMembershipRepository.findByTripId(100L)).thenReturn(List.of());

        // 80% already sent, 100% not yet sent
        when(notificationRepository.existsByTripIdAndTypeAndReferenceId(100, NotificationType.BUDGET_ALERT, 80L))
                .thenReturn(true);
        when(notificationRepository.existsByTripIdAndTypeAndReferenceId(100, NotificationType.BUDGET_ALERT, 100L))
                .thenReturn(false);

        expenseService.updateExpense(100L, 1L, updatedExpense, owner);

        // 80% not sent again
        verify(notificationService, never()).createNotification(
                any(), anyString(), eq(NotificationType.BUDGET_ALERT), eq(trip), eq(80L));
        // 100% sent
        verify(notificationService, times(1)).createNotification(
                eq(owner), contains("100%"), eq(NotificationType.BUDGET_ALERT), eq(trip), eq(100L));
    }

    @Test
    void testExpense_DoesNotDuplicateAlert_WhenAlreadySent() {
        Expense expense = new Expense();
        expense.setTripId(100L);
        expense.setBudgetId(5L);
        expense.setPayerId(1L);
        expense.setCategory("FOOD");
        expense.setAmount(BigDecimal.valueOf(105000));
        expense.setExpenseDate(LocalDate.now());

        when(budgetRepository.findByTripId(100L)).thenReturn(Optional.of(budget));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        when(tripRepository.findById(100)).thenReturn(Optional.of(trip));
        when(expenseRepository.getTotalExpenses(100L)).thenReturn(BigDecimal.valueOf(105000)); // 105%

        // Both 80% and 100% already sent
        when(notificationRepository.existsByTripIdAndTypeAndReferenceId(100, NotificationType.BUDGET_ALERT, 80L))
                .thenReturn(true);
        when(notificationRepository.existsByTripIdAndTypeAndReferenceId(100, NotificationType.BUDGET_ALERT, 100L))
                .thenReturn(true);

        expenseService.createExpense(expense, owner);

        // No alerts should be dispatched
        verify(notificationService, never()).createNotification(any(), anyString(), any(), any(), anyLong());
    }
}
