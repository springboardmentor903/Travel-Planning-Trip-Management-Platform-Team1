package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Expense;
import com.tripnest.tripnest_backend.entity.Budget;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.ExpenseRepository;
import com.tripnest.tripnest_backend.repository.BudgetRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final TripAccessService tripAccessService;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            BudgetRepository budgetRepository,
            TripAccessService tripAccessService) {

        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
        this.tripAccessService = tripAccessService;
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

        return expenseRepository.save(expense);
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

        return expenseRepository.save(existingExpense);
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
                "Transportation",
                "Hotel",
                "Food",
                "Shopping",
                "Entertainment",
                "Miscellaneous"
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