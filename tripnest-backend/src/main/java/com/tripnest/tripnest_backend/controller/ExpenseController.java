package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.entity.Expense;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.UserRepository;
import com.tripnest.tripnest_backend.service.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserRepository userRepository;

    public ExpenseController(
            ExpenseService expenseService,
            UserRepository userRepository) {

        this.expenseService = expenseService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication authentication) {

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // =====================================================
    // CREATE EXPENSE
    // =====================================================

    @PostMapping
    public ResponseEntity<Expense> createExpense(
            @RequestBody Expense expense,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        Expense createdExpense =
                expenseService.createExpense(
                        expense,
                        currentUser
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdExpense);
    }

    // =====================================================
    // LIST EXPENSES
    // =====================================================

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<Expense>> getExpenses(
            @PathVariable Long tripId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        return ResponseEntity.ok(
                expenseService.getExpensesByTripId(
                        tripId,
                        currentUser
                )
        );
    }

    // =====================================================
    // GET SINGLE EXPENSE
    // =====================================================

    @GetMapping("/{expenseId}/trip/{tripId}")
    public ResponseEntity<Expense> getExpense(
            @PathVariable Long expenseId,
            @PathVariable Long tripId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        return ResponseEntity.ok(
                expenseService.getExpense(
                        tripId,
                        expenseId,
                        currentUser
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
            @RequestBody Expense expense,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        return ResponseEntity.ok(
                expenseService.updateExpense(
                        tripId,
                        expenseId,
                        expense,
                        currentUser
                )
        );
    }

    // =====================================================
    // DELETE EXPENSE
    // =====================================================

    @DeleteMapping("/{expenseId}/trip/{tripId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long expenseId,
            @PathVariable Long tripId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        expenseService.deleteExpense(
                tripId,
                expenseId,
                currentUser
        );

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // CATEGORY SUMMARY
    // =====================================================

    @GetMapping("/trip/{tripId}/category-summary")
    public ResponseEntity<List<java.util.Map<String, Object>>> categorySummary(
            @PathVariable Long tripId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        List<Object[]> rawSummary = expenseService.getCategorySummary(
                tripId,
                currentUser
        );

        List<java.util.Map<String, Object>> summary = rawSummary.stream().map(row -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("category", row[0]);
            map.put("totalAmount", row[1]);
            return map;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(summary);
    }

    // =====================================================
    // TOTAL EXPENSE
    // =====================================================

    @GetMapping("/trip/{tripId}/total")
    public ResponseEntity<BigDecimal> totalExpenses(
            @PathVariable Long tripId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        return ResponseEntity.ok(
                expenseService.getTotalExpenses(
                        tripId,
                        currentUser
                )
        );
    }

    // =====================================================
    // REMAINING BUDGET
    // =====================================================

    @GetMapping("/trip/{tripId}/remaining-budget")
    public ResponseEntity<BigDecimal> remainingBudget(
            @PathVariable Long tripId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        return ResponseEntity.ok(
                expenseService.getRemainingBudget(
                        tripId,
                        currentUser
                )
        );
    }
}