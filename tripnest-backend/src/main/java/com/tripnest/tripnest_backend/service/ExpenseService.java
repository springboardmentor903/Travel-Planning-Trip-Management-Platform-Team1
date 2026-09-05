package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Budget;
import com.tripnest.tripnest_backend.entity.Expense;
import com.tripnest.tripnest_backend.entity.NotificationType;
import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.TripMembership;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.BudgetRepository;
import com.tripnest.tripnest_backend.repository.ExpenseRepository;
import com.tripnest.tripnest_backend.repository.NotificationRepository;
import com.tripnest.tripnest_backend.repository.TripMembershipRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Slf4j
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final TripAccessService tripAccessService;
    private final TripRepository tripRepository;
    private final TripMembershipRepository tripMembershipRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            BudgetRepository budgetRepository,
            TripAccessService tripAccessService,
            TripRepository tripRepository,
            TripMembershipRepository tripMembershipRepository,
            NotificationService notificationService,
            NotificationRepository notificationRepository) {

        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
        this.tripAccessService = tripAccessService;
        this.tripRepository = tripRepository;
        this.tripMembershipRepository = tripMembershipRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    // =====================================================
    // CREATE EXPENSE
    // =====================================================

    public Expense createExpense(
            Expense expense,
            User currentUser) {

        validateExpense(expense);

        tripAccessService.checkAccess(
                expense.getTripId(),
                currentUser
        );

        Budget budget = budgetRepository
                .findByTripId(expense.getTripId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Budget not found for this trip"
                        )
                );

        if (!budget.getId().equals(expense.getBudgetId())) {
            throw new RuntimeException(
                    "Budget does not belong to this trip"
            );
        }

        Expense saved = expenseRepository.save(expense);

        // Budget alert check (80% and 100%)
        checkAndSendBudgetAlerts(saved.getTripId());

        return saved;
    }

    // =====================================================
    // GET ALL EXPENSES
    // =====================================================

    public List<Expense> getExpensesByTripId(
            Long tripId,
            User currentUser) {

        tripAccessService.checkAccess(
                tripId,
                currentUser
        );

        return expenseRepository.findByTripId(tripId);
    }

    // =====================================================
    // GET SINGLE EXPENSE
    // =====================================================

    public Expense getExpense(
            Long tripId,
            Long expenseId,
            User currentUser) {

        tripAccessService.checkAccess(
                tripId,
                currentUser
        );

        return expenseRepository
                .findByIdAndTripId(expenseId, tripId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Expense not found"
                        )
                );
    }

    // =====================================================
    // UPDATE EXPENSE
    // =====================================================

    public Expense updateExpense(
            Long tripId,
            Long expenseId,
            Expense updatedExpense,
            User currentUser) {

        tripAccessService.checkAccess(
                tripId,
                currentUser
        );

        validateExpense(updatedExpense);

        Expense existingExpense =
                expenseRepository
                        .findByIdAndTripId(
                                expenseId,
                                tripId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Expense not found"
                                )
                        );

        Budget budget = budgetRepository
                .findByTripId(tripId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Budget not found for this trip"
                        )
                );

        if (!budget.getId().equals(updatedExpense.getBudgetId())) {
            throw new RuntimeException(
                    "Budget does not belong to this trip"
            );
        }

        existingExpense.setCategory(
                updatedExpense.getCategory()
        );

        existingExpense.setAmount(
                updatedExpense.getAmount()
        );

        existingExpense.setExpenseDate(
                updatedExpense.getExpenseDate()
        );

        existingExpense.setReceiptLink(
                updatedExpense.getReceiptLink()
        );

        existingExpense.setPayerId(
                updatedExpense.getPayerId()
        );

        existingExpense.setBudgetId(
                updatedExpense.getBudgetId()
        );

        Expense saved = expenseRepository.save(existingExpense);

        // Budget alert check (80% and 100%)
        checkAndSendBudgetAlerts(saved.getTripId());

        return saved;
    }

    // =====================================================
    // BUDGET ALERT CHECK (80% & 100% WITH PERSISTENT DEDUP)
    // =====================================================

    private void checkAndSendBudgetAlerts(Long tripId) {
        if (tripId == null) {
            return;
        }

        try {
            Trip trip = tripRepository.findById(tripId.intValue()).orElse(null);
            if (trip == null) {
                return;
            }

            Optional<Budget> budgetOpt = budgetRepository.findByTripId(tripId);
            BigDecimal totalBudget = null;

            if (budgetOpt.isPresent() && budgetOpt.get().getTotalBudget() != null
                    && budgetOpt.get().getTotalBudget().compareTo(BigDecimal.ZERO) > 0) {
                totalBudget = budgetOpt.get().getTotalBudget();
            } else if (trip.getBudget() != null && trip.getBudget() > 0) {
                totalBudget = BigDecimal.valueOf(trip.getBudget());
            }

            if (totalBudget == null || totalBudget.compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }

            BigDecimal totalExpenses = expenseRepository.getTotalExpenses(tripId);
            if (totalExpenses == null) {
                totalExpenses = BigDecimal.ZERO;
            }

            BigDecimal percentage = totalExpenses
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalBudget, 2, RoundingMode.HALF_UP);

            String destName = (trip.getDestination() != null && trip.getDestination().getName() != null)
                    ? trip.getDestination().getName()
                    : "Trip #" + trip.getId();

            Set<User> recipients = new HashSet<>();
            if (trip.getUser() != null) {
                recipients.add(trip.getUser());
            }
            List<TripMembership> memberships = tripMembershipRepository.findByTripId(tripId);
            for (TripMembership tm : memberships) {
                if (tm.getUser() != null) {
                    recipients.add(tm.getUser());
                }
            }

            // Threshold 80%
            if (percentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
                boolean alert80Sent = notificationRepository.existsByTripIdAndTypeAndReferenceId(
                        trip.getId(), NotificationType.BUDGET_ALERT, 80L);
                if (!alert80Sent) {
                    String msg80 = "Your trip budget for " + destName + " has reached 80%.";
                    for (User recipient : recipients) {
                        try {
                            notificationService.createNotification(recipient, msg80, NotificationType.BUDGET_ALERT, trip, 80L);
                        } catch (Exception ex) {
                            log.warn("Failed to send 80% budget alert to {}: {}", recipient.getEmail(), ex.getMessage());
                        }
                    }
                    log.info("80% budget alert sent for trip {}", trip.getId());
                }
            }

            // Threshold 100%
            if (percentage.compareTo(BigDecimal.valueOf(100)) >= 0) {
                boolean alert100Sent = notificationRepository.existsByTripIdAndTypeAndReferenceId(
                        trip.getId(), NotificationType.BUDGET_ALERT, 100L);
                if (!alert100Sent) {
                    String msg100 = "Your trip budget for " + destName + " has reached 100%.";
                    for (User recipient : recipients) {
                        try {
                            notificationService.createNotification(recipient, msg100, NotificationType.BUDGET_ALERT, trip, 100L);
                        } catch (Exception ex) {
                            log.warn("Failed to send 100% budget alert to {}: {}", recipient.getEmail(), ex.getMessage());
                        }
                    }
                    log.info("100% budget alert sent for trip {}", trip.getId());
                }
            }
        } catch (Exception e) {
            log.error("Error evaluating budget alerts for trip {}: {}", tripId, e.getMessage(), e);
        }
    }

    // =====================================================
    // DELETE EXPENSE
    // =====================================================

    public void deleteExpense(
            Long tripId,
            Long expenseId,
            User currentUser) {

        tripAccessService.checkAccess(
                tripId,
                currentUser
        );

        Expense expense =
                expenseRepository
                        .findByIdAndTripId(
                                expenseId,
                                tripId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Expense not found"
                                )
                        );

        expenseRepository.delete(expense);
    }

    // =====================================================
    // CATEGORY SUMMARY
    // =====================================================

    public List<Object[]> getCategorySummary(
            Long tripId,
            User currentUser) {

        tripAccessService.checkAccess(
                tripId,
                currentUser
        );

        return expenseRepository
                .getCategorySummary(tripId);
    }

    // =====================================================
    // TOTAL EXPENSE
    // =====================================================

    public BigDecimal getTotalExpenses(
            Long tripId,
            User currentUser) {

        tripAccessService.checkAccess(
                tripId,
                currentUser
        );

        BigDecimal total =
                expenseRepository
                        .getTotalExpenses(tripId);

        return total != null
                ? total
                : BigDecimal.ZERO;
    }

    // =====================================================
    // REMAINING BUDGET
    // =====================================================

    public BigDecimal getRemainingBudget(
            Long tripId,
            User currentUser) {

        tripAccessService.checkAccess(
                tripId,
                currentUser
        );

        Optional<Budget> budgetOpt =
                budgetRepository.findByTripId(tripId);

        if (budgetOpt.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Budget budget = budgetOpt.get();

        BigDecimal totalBudget =
                budget.getTotalBudget() != null
                        ? budget.getTotalBudget()
                        : BigDecimal.ZERO;

        BigDecimal totalExpense =
                getTotalExpenses(tripId, currentUser);

        return totalBudget.subtract(totalExpense);
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateExpense(Expense expense) {

        if (expense.getTripId() == null) {
            throw new RuntimeException(
                    "Trip ID is required"
            );
        }

        if (expense.getBudgetId() == null) {
            throw new RuntimeException(
                    "Budget ID is required"
            );
        }

        if (expense.getPayerId() == null) {
            throw new RuntimeException(
                    "Payer ID is required"
            );
        }

        if (expense.getCategory() == null ||
                expense.getCategory().trim().isEmpty()) {

            throw new RuntimeException(
                    "Expense category is required"
            );
        }

        if (expense.getAmount() == null ||
                expense.getAmount()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Expense amount must be greater than zero"
            );
        }

        if (expense.getExpenseDate() == null) {
            throw new RuntimeException(
                    "Expense date is required"
            );
        }

        validateCategory(expense.getCategory());
    }

    // =====================================================
    // CATEGORY VALIDATION
    // =====================================================

    private void validateCategory(String category) {

        List<String> allowedCategories = List.of(
                "ACCOMMODATION",
                "FOOD",
                "TRANSPORT",
                "ACTIVITIES",
                "SHOPPING",
                "MISCELLANEOUS"
        );

        if (!allowedCategories.contains(category)) {

            throw new RuntimeException(
                    "Invalid expense category. Allowed categories: "
                            + String.join(
                                    ", ",
                                    allowedCategories
                            )
            );
        }
    }
}