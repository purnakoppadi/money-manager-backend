package com.purnakoppadi.moneymanager.controller;


import com.purnakoppadi.moneymanager.dto.ExpenseDTO;
import com.purnakoppadi.moneymanager.dto.FilterDTO;
import com.purnakoppadi.moneymanager.dto.IncomeDTO;
import com.purnakoppadi.moneymanager.service.ExpenseService;
import com.purnakoppadi.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;


    @PostMapping
    public ResponseEntity<?> filterTransactions(@RequestBody FilterDTO filterDTO)
    {
        LocalDate startDate=filterDTO.getStartDate()!=null ? filterDTO.getStartDate():LocalDate.MIN;
        LocalDate endDate=filterDTO.getEndDate()!=null ? filterDTO.getEndDate():LocalDate.now();
        String keyword=filterDTO.getKeyword()!=null ? filterDTO.getKeyword() : "";
        String sortField=filterDTO.getSortField()!=null ?filterDTO.getSortField():"date";
        Sort.Direction direction="desc".equalsIgnoreCase(filterDTO.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort=Sort.by(direction,sortField);

        if("income".equalsIgnoreCase(filterDTO.getType()))
        {
           List<IncomeDTO> income= incomeService.filterIncomes(startDate,endDate,keyword,sort);
           return ResponseEntity.ok(income);
        }
        else if("expense".equalsIgnoreCase(filterDTO.getType()))
        {
            List<ExpenseDTO> expenseDTOS=expenseService.filterExpenses(startDate,endDate,keyword,sort);
            return ResponseEntity.ok(expenseDTOS);
        }
        else {
            return ResponseEntity.badRequest().body("Invalid type Must be income or expense ");
        }
    }

}
