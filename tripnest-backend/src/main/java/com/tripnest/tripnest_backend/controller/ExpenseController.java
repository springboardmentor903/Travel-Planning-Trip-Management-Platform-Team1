package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.entity.Expense;
import com.tripnest.tripnest_backend.service.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(
            ExpenseService expenseService) {

        this.expenseService = expenseService;
    }


    // =====================================================
    // CREATE EXPENSE
    // =====================================================

    @PostMapping
    public ResponseEntity<Expense> createExpense(
            @RequestBody Expense expense) {

        Expense createdExpense =
                expenseService.createExpense(expense);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdExpense);
    }


    // =====================================================
    // LIST EXPENSES
    // =====================================================

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<Expense>> getExpenses(
            @PathVariable Long tripId) {

        return ResponseEntity.ok(
                expenseService.getExpensesByTripId(
                        tripId
                )
        );
    }


    // =====================================================
    // GET SINGLE EXPENSE
    // =====================================================

    @GetMapping("/{expenseId}/trip/{tripId}")
    public ResponseEntity<Expense> getExpense(
            @PathVariable Long expenseId,
            @PathVariable Long tripId) {

        return ResponseEntity.ok(
                expenseService.getExpense(
                        tripId,
                        expenseId
                )
        );
    }


    // =====================================================
    // UPDATE EXPENSE
    // =====================================================

    @PutMapping("/{expenseId}/trip/{tripId}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable Long expenseId,
            @PathVariable Long tripId,
            @RequestBody Expense expense) {

        return ResponseEntity.ok(
                expenseService.updateExpense(
                        tripId,
                        expenseId,
                        expense
                )
        );
    }


    // =====================================================
    // DELETE EXPENSE
    // =====================================================

    @DeleteMapping("/{expenseId}/trip/{tripId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long expenseId,
            @PathVariable Long tripId) {

        expenseService.deleteExpense(
                tripId,
                expenseId
        );

        return ResponseEntity.noContent().build();
    }


    // =====================================================
    // CATEGORY SUMMARY
    // =====================================================

    @GetMapping("/trip/{tripId}/category-summary")
    public ResponseEntity<List<Object[]>> categorySummary(
            @PathVariable Long tripId) {

        return ResponseEntity.ok(
                expenseService.getCategorySummary(
                        tripId
                )
        );
    }


    // =====================================================
    // TOTAL EXPENSE
    // =====================================================

    @GetMapping("/trip/{tripId}/total")
    public ResponseEntity<BigDecimal> totalExpenses(
            @PathVariable Long tripId) {

        return ResponseEntity.ok(
                expenseService.getTotalExpenses(
                        tripId
                )
        );
    }


    // =====================================================
    // REMAINING BUDGET
    // =====================================================

    @GetMapping("/trip/{tripId}/remaining-budget")
    public ResponseEntity<BigDecimal> remainingBudget(
            @PathVariable Long tripId) {

        return ResponseEntity.ok(
                expenseService.getRemainingBudget(
                        tripId
                )
        );
    }
}