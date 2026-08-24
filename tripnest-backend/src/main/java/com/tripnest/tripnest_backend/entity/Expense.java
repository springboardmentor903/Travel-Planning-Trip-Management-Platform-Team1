package com.tripnest.tripnest_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Trip ID
    @Column(nullable = false)
    private Long tripId;

    // Budget ID
    @Column(nullable = false)
    private Long budgetId;

    // User who paid the expense
    @Column(nullable = false)
    private Long payerId;

    // Expense category
    @Column(nullable = false)
    private String category;

    // Amount
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    // Expense date
    @Column(nullable = false)
    private LocalDate expenseDate;

    // Optional receipt URL
    private String receiptLink;


    public Expense() {
    }


    public Long getId() {
        return id;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public Long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Long budgetId) {
        this.budgetId = budgetId;
    }

    public Long getPayerId() {
        return payerId;
    }

    public void setPayerId(Long payerId) {
        this.payerId = payerId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getReceiptLink() {
        return receiptLink;
    }

    public void setReceiptLink(String receiptLink) {
        this.receiptLink = receiptLink;
    }
}