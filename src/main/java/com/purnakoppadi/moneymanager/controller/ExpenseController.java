package com.purnakoppadi.moneymanager.controller;


import com.purnakoppadi.moneymanager.dto.ExpenseDTO;
import com.purnakoppadi.moneymanager.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;


    @PostMapping
    public ResponseEntity<ExpenseDTO> addExpense(@RequestBody ExpenseDTO expenseDTO)
    {
        ExpenseDTO expenseDTO1=expenseService.addExpense(expenseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseDTO1);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDTO>>  getExpenses()
    {
        List<ExpenseDTO> expenseDTOS=expenseService.getCurrentMonthExpensesForCurrentUser();

        return ResponseEntity.ok(expenseDTOS);


    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id)
    {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }



}
