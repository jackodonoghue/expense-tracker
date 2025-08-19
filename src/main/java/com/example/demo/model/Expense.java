package com.example.demo.model;

public class Expense {
    private Long id;
    private String description;
    private Double amount;

    public Expense() {}

    public Expense(Long id, String description, Double amount) {
        this.id = id;
        this.description = description;
        this.amount = amount;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}
