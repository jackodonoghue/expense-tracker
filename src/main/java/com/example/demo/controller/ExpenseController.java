package com.example.demo.controller;

import com.example.demo.model.Expense;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final List<Expense> expenses = new ArrayList<>();

    @GetMapping
    public List<Expense> getAllExpenses() {
        return expenses;
    }

    @PostMapping
    public Expense addExpense(@RequestBody Expense expense) {
        expense.setId((long) (expenses.size() + 1));
        expenses.add(expense);
        return expense;
    }
}
