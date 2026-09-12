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
@RequestMapping("/excel")
public class ExcelController {

    private final IncomeService incomeService;

    private final ExpenseService expenseService;

    @GetMapping("/download/income")
    public ResponseEntity<byte[]> downloadIncomeExcel() {

        byte[] data = incomeService.downloadIncomeExcel();

        return ResponseEntity.ok()
                .header(
                        "Content-Disposition",
                        "attachment; filename=income_details.xlsx"
                )
                .header(
                        "Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
                .body(data);
    }


    @GetMapping("/download/expense")
    public ResponseEntity<byte[]> downloadExpenseExcel() {

        byte[] data = expenseService.downloadExpenseExcel();

        return ResponseEntity.ok()
                .header(
                        "Content-Disposition",
                        "attachment; filename=expense_details.xlsx"
                )
                .header(
                        "Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
                .body(data);
    }
}