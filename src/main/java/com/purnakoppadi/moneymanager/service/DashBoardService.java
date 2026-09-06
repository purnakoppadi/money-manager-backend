package com.purnakoppadi.moneymanager.service;

import com.purnakoppadi.moneymanager.dto.ExpenseDTO;
import com.purnakoppadi.moneymanager.dto.IncomeDTO;
import com.purnakoppadi.moneymanager.dto.RecentTransactionDTO;
import com.purnakoppadi.moneymanager.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DashBoardService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;

    public Map<String, Object> getDashBoard()
    {
        ProfileEntity profile = profileService.getCurrentProfile();

        Map<String, Object> returnValue = new LinkedHashMap<>();

        List<IncomeDTO> latestIncomes =
                incomeService.getLatestFiveIncomesForCurrentUser();

        List<ExpenseDTO> latestExpenses =
                expenseService.getLatestFiveExpensesForCurrentUser();

       List<RecentTransactionDTO> recentTransactions= Stream.concat(
                latestIncomes.stream().map(income ->
                        RecentTransactionDTO.builder()
                                .id(income.getId())
                                .profileId(profile.getId())
                                .icon(income.getIcon())
                                .name(income.getName())
                                .amount(income.getAmount())
                                .date(income.getDate())
                                .createdAt(income.getCreatedAt())
                                .updatedAt(income.getUpdatedAt())
                                .type("income")
                                .build()
                ),

                latestExpenses.stream().map(expense ->
                        RecentTransactionDTO.builder()
                                .id(expense.getId())
                                .profileId(profile.getId())
                                .icon(expense.getIcon())
                                .name(expense.getName())
                                .amount(expense.getAmount())
                                .date(expense.getDate())
                                .createdAt(expense.getCreatedAt())
                                .updatedAt(expense.getUpdatedAt())
                                .type("expense")
                                .build()
                )
        ).sorted((a, b) -> {

           if (a.getDate() == null && b.getDate() == null) {
               return 0;
           }

           if (a.getDate() == null) {
               return 1;
           }

           if (b.getDate() == null) {
               return -1;
           }

           int cmp = b.getDate().compareTo(a.getDate());

           if (cmp == 0 &&
                   a.getCreatedAt() != null &&
                   b.getCreatedAt() != null) {

               return b.getCreatedAt().compareTo(a.getCreatedAt());
           }

           return cmp;
       }).collect(Collectors.toUnmodifiableList());

       returnValue.put("totalBalance",incomeService.getTotalIncomeForCurrentUser()
               .subtract(expenseService.getTotalExpenseForCurrentUser()));
       returnValue.put("TotalIncome",incomeService.getTotalIncomeForCurrentUser());
       returnValue.put("totalExpenses",expenseService.getTotalExpenseForCurrentUser());
       returnValue.put("recent5Expenses",latestExpenses);
       returnValue.put("recent5Incomes",latestIncomes);
       returnValue.put("recentTransactions",recentTransactions);

       return returnValue;




    }
}