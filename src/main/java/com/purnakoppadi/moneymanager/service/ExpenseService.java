package com.purnakoppadi.moneymanager.service;


import com.purnakoppadi.moneymanager.dto.ExpenseDTO;
import com.purnakoppadi.moneymanager.entity.CategoryEntity;
import com.purnakoppadi.moneymanager.entity.ExpenseEntity;
import com.purnakoppadi.moneymanager.entity.ProfileEntity;
import com.purnakoppadi.moneymanager.repository.CategoryRepository;
import com.purnakoppadi.moneymanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;

    public ExpenseDTO addExpense(ExpenseDTO expenseDTO)
    {
        ProfileEntity entity=profileService.getCurrentProfile();
        CategoryEntity category=categoryRepository.findById(expenseDTO.getCategoryId())
                .orElseThrow(()->new RuntimeException("Category not found"));

        ExpenseEntity newExpense=toEntity(expenseDTO,entity,category);
        newExpense=expenseRepository.save(newExpense);

        return toDTO(newExpense);

    }

    public List<ExpenseDTO> getCurrentMonthExpensesForCurrentUser()
    {
        ProfileEntity profile=profileService.getCurrentProfile();
        LocalDate now=LocalDate.now();
        LocalDate startDate=now.withDayOfMonth(1);
        LocalDate endDate=now.withDayOfMonth(now.lengthOfMonth());
        List<ExpenseEntity> list=expenseRepository.findByProfileIdAndDateBetween(profile.getId(),startDate,endDate);
        return list.stream().map(this::toDTO).toList();

   }

   public  void deleteExpense(Long expenseId)
   {
       ProfileEntity profile=profileService.getCurrentProfile();
       ExpenseEntity entity=expenseRepository.findById(expenseId)
               .orElseThrow(()->new RuntimeException("Expense not found"));
       if(!entity.getProfile().getId().equals(profile.getId()))
       {
           throw new RuntimeException("Inauthorized to delete this expense");
       }

       expenseRepository.delete(entity);
   }

   public List<ExpenseDTO> getLatestFiveExpensesForCurrentUser()
   {
       ProfileEntity profile=profileService.getCurrentProfile();
       List<ExpenseEntity> expenseEntities= expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());

       return expenseEntities.stream().map(this::toDTO).toList();
   }

   public BigDecimal getTotalExpenseForCurrentUser()
   {
       ProfileEntity entity=profileService.getCurrentProfile();
       BigDecimal total=expenseRepository.findTotalExpenseByProfileId(entity.getId());

       return total!=null ? total:BigDecimal.ZERO;
   }

   public List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort)
   {
        ProfileEntity profile=profileService.getCurrentProfile();

        List<ExpenseEntity> list=expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(),startDate,endDate,keyword,sort);
        return list.stream().map(this::toDTO).toList();
   }



    private ExpenseEntity toEntity(
            ExpenseDTO expenseDTO,
            ProfileEntity profile,
            CategoryEntity category)
    {
        return ExpenseEntity.builder()
                .name(expenseDTO.getName())
                .icon(expenseDTO.getIcon())
                .amount(expenseDTO.getAmount())
                .date(expenseDTO.getDate() != null
                        ? expenseDTO.getDate()
                        : LocalDate.now())
                .profile(profile)
                .category(category)
                .build();
    }

    private ExpenseDTO toDTO(ExpenseEntity entity)
    {
        return ExpenseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .categoryId(entity.getCategory() != null
                        ? entity.getCategory().getId()
                        : null)
                .categoryName(entity.getCategory() != null
                        ? entity.getCategory().getName()
                        : "N/A")
                .amount(entity.getAmount())
                .date(entity.getDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<ExpenseDTO> getExpensesForUserOnDate(Long profileId,LocalDate date)
    {
       List<ExpenseEntity> list= expenseRepository.findByProfileIdAndDate(profileId,date);
       return  list.stream().map(this::toDTO).toList();
    }


}
