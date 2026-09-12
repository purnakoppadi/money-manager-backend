package com.purnakoppadi.moneymanager.service;


import com.purnakoppadi.moneymanager.dto.ExpenseDTO;
import com.purnakoppadi.moneymanager.entity.CategoryEntity;
import com.purnakoppadi.moneymanager.entity.ExpenseEntity;
import com.purnakoppadi.moneymanager.entity.ProfileEntity;
import com.purnakoppadi.moneymanager.repository.CategoryRepository;
import com.purnakoppadi.moneymanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;

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
    private final EmailService emailService;

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

    public byte[] downloadExpenseExcel() {

        ProfileEntity profile = profileService.getCurrentProfile();

        List<ExpenseEntity> expenses =
                expenseRepository.findByProfileId(profile.getId());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Expense Details");

            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Amount");
            header.createCell(3).setCellValue("Category");
            header.createCell(4).setCellValue("Date");

            int rowNum = 1;

            for (ExpenseEntity expense : expenses) {

                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(expense.getId());

                row.createCell(1).setCellValue(
                        expense.getName()
                );

                row.createCell(2).setCellValue(
                        expense.getAmount().doubleValue()
                );

                row.createCell(3).setCellValue(
                        expense.getCategory() != null
                                ? expense.getCategory().getName()
                                : "N/A"
                );

                row.createCell(4).setCellValue(
                        expense.getDate() != null
                                ? expense.getDate().toString()
                                : ""
                );
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate expense Excel",
                    e
            );
        }
    }


    public void emailExpenseDetails() {

        ProfileEntity profile = profileService.getCurrentProfile();

        List<ExpenseEntity> expenses =
                expenseRepository.findByProfileId(profile.getId());

        StringBuilder table = new StringBuilder();

        table.append(
                "<table style='border-collapse:collapse;width:100%;'>"
        );

        table.append(
                "<tr style='background-color:#f2f2f2;'>" +
                        "<th style='border:1px solid #ddd;padding:8px;'>S.No</th>" +
                        "<th style='border:1px solid #ddd;padding:8px;'>Name</th>" +
                        "<th style='border:1px solid #ddd;padding:8px;'>Amount</th>" +
                        "<th style='border:1px solid #ddd;padding:8px;'>Category</th>" +
                        "<th style='border:1px solid #ddd;padding:8px;'>Date</th>" +
                        "</tr>"
        );

        int i = 1;

        for (ExpenseEntity expense : expenses) {

            table.append("<tr>");

            table.append(
                            "<td style='border:1px solid #ddd;padding:8px;'>"
                    )
                    .append(i++)
                    .append("</td>");

            table.append(
                            "<td style='border:1px solid #ddd;padding:8px;'>"
                    )
                    .append(expense.getName())
                    .append("</td>");

            table.append(
                            "<td style='border:1px solid #ddd;padding:8px;'>"
                    )
                    .append(expense.getAmount())
                    .append("</td>");

            table.append(
                            "<td style='border:1px solid #ddd;padding:8px;'>"
                    )
                    .append(
                            expense.getCategory() != null
                                    ? expense.getCategory().getName()
                                    : "N/A"
                    )
                    .append("</td>");

            table.append(
                            "<td style='border:1px solid #ddd;padding:8px;'>"
                    )
                    .append(
                            expense.getDate() != null
                                    ? expense.getDate()
                                    : ""
                    )
                    .append("</td>");

            table.append("</tr>");
        }

        table.append("</table>");

        String body =
                "Hi " + profile.getFullName() +
                        ",<br><br>" +
                        "Here are your expense details:<br><br>" +
                        table +
                        "<br><br>" +
                        "Best regards,<br>" +
                        "Money Manager Team";

        emailService.sendEmail(
                profile.getEmail(),
                "Your Expense Details",
                body
        );
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
