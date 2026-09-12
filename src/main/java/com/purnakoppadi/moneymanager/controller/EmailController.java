package com.purnakoppadi.moneymanager.controller;

import com.purnakoppadi.moneymanager.service.ExpenseService;
import com.purnakoppadi.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/email")
public class EmailController {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    @GetMapping("/income")
    public ResponseEntity<String> emailIncomeDetails() {

        incomeService.emailIncomeDetails();

        return ResponseEntity.ok("Income details emailed successfully");
    }

    @GetMapping("/expense")
    public ResponseEntity<String> emailExpenseDetails() {

        expenseService.emailExpenseDetails();

        return ResponseEntity.ok(
                "Expense details emailed successfully"
        );
    }
}