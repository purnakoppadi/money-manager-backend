package com.purnakoppadi.moneymanager.controller;

import com.purnakoppadi.moneymanager.dto.ExpenseDTO;
import com.purnakoppadi.moneymanager.dto.IncomeDTO;
import com.purnakoppadi.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<IncomeDTO> addIncome(
            @RequestBody IncomeDTO incomeDTO) {

        IncomeDTO incomeDTO1 = incomeService.addIncome(incomeDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(incomeDTO1);
    }


    @GetMapping
    public ResponseEntity<List<IncomeDTO>>  getIncomes()
    {
        List<IncomeDTO> incomeDTOS=incomeService.getCurrentMonthIncomeForCurrentUser();

        return ResponseEntity.ok(incomeDTOS);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id)
    {
        incomeService.deleteIncome(id);
        return ResponseEntity.noContent().build();
    }
}