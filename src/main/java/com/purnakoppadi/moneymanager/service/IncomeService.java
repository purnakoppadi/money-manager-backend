package com.purnakoppadi.moneymanager.service;

import com.purnakoppadi.moneymanager.dto.ExpenseDTO;
import com.purnakoppadi.moneymanager.dto.IncomeDTO;
import com.purnakoppadi.moneymanager.entity.CategoryEntity;
import com.purnakoppadi.moneymanager.entity.ExpenseEntity;
import com.purnakoppadi.moneymanager.entity.IncomeEntity;
import com.purnakoppadi.moneymanager.entity.ProfileEntity;
import com.purnakoppadi.moneymanager.repository.CategoryRepository;
import com.purnakoppadi.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;
    private final EmailService emailService;


    // Add Income
    public IncomeDTO addIncome(IncomeDTO incomeDTO) {

        ProfileEntity profile = profileService.getCurrentProfile();

        CategoryEntity category = categoryRepository
                .findById(incomeDTO.getCategoryId())
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));

        IncomeEntity newIncome =
                toEntity(incomeDTO, profile, category);

        newIncome = incomeRepository.save(newIncome);

        return toDTO(newIncome);
    }


    // Get current month's income
    public List<IncomeDTO> getCurrentMonthIncomeForCurrentUser() {

        ProfileEntity profile =
                profileService.getCurrentProfile();

        LocalDate now = LocalDate.now();

        LocalDate startDate =
                now.withDayOfMonth(1);

        LocalDate endDate =
                now.withDayOfMonth(now.lengthOfMonth());

        List<IncomeEntity> incomes =
                incomeRepository.findByProfileIdAndDateBetween(
                        profile.getId(),
                        startDate,
                        endDate
                );

        return incomes.stream()
                .map(this::toDTO)
                .toList();
    }


    // Get latest 5 incomes
    public List<IncomeDTO> getLatestFiveIncomesForCurrentUser() {

        ProfileEntity profile =
                profileService.getCurrentProfile();

        List<IncomeEntity> incomeEntities =
                incomeRepository
                        .findTop5ByProfileIdOrderByDateDesc(
                                profile.getId());

        return incomeEntities.stream()
                .map(this::toDTO)
                .toList();
    }


    // Delete income
    public void deleteIncome(Long incomeId) {

        ProfileEntity profile =
                profileService.getCurrentProfile();

        IncomeEntity entity =
                incomeRepository.findById(incomeId)
                        .orElseThrow(() ->
                                new RuntimeException("Income not found"));

        if (!entity.getProfile().getId().equals(profile.getId())) {

            throw new RuntimeException(
                    "Unauthorized to delete this income");
        }

        incomeRepository.delete(entity);
    }


    // Get total income
    public BigDecimal getTotalIncomeForCurrentUser() {

        ProfileEntity profile =
                profileService.getCurrentProfile();

        BigDecimal total =
                incomeRepository
                        .findTotalIncomeByProfileId(
                                profile.getId());

        return total != null
                ? total
                : BigDecimal.ZERO;
    }

    public List<IncomeDTO> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort)
    {
        ProfileEntity profile=profileService.getCurrentProfile();

        List<IncomeEntity> list=incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(),startDate,endDate,keyword,sort);
        return list.stream().map(this::toDTO).toList();
    }

    public byte[] downloadIncomeExcel() {

        ProfileEntity profile = profileService.getCurrentProfile();

        List<IncomeEntity> incomes =
                incomeRepository.findByProfileId(profile.getId());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Income Details");

            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Amount");
            header.createCell(3).setCellValue("Category");
            header.createCell(4).setCellValue("Date");

            int rowNum = 1;

            for (IncomeEntity income : incomes) {

                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(income.getId());
                row.createCell(1).setCellValue(income.getName());
                row.createCell(2).setCellValue(
                        income.getAmount().doubleValue()
                );
                row.createCell(3).setCellValue(
                        income.getCategory() != null
                                ? income.getCategory().getName()
                                : "N/A"
                );
                row.createCell(4).setCellValue(
                        income.getDate() != null
                                ? income.getDate().toString()
                                : ""
                );
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate income Excel", e);
        }
    }

    public void emailIncomeDetails() {

        ProfileEntity profile = profileService.getCurrentProfile();

        List<IncomeEntity> incomes =
                incomeRepository.findByProfileId(profile.getId());

        StringBuilder table = new StringBuilder();

        table.append("<table style='border-collapse:collapse;width:100%;'>");

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

        for (IncomeEntity income : incomes) {

            table.append("<tr>");

            table.append("<td style='border:1px solid #ddd;padding:8px;'>")
                    .append(i++)
                    .append("</td>");

            table.append("<td style='border:1px solid #ddd;padding:8px;'>")
                    .append(income.getName())
                    .append("</td>");

            table.append("<td style='border:1px solid #ddd;padding:8px;'>")
                    .append(income.getAmount())
                    .append("</td>");

            table.append("<td style='border:1px solid #ddd;padding:8px;'>")
                    .append(
                            income.getCategory() != null
                                    ? income.getCategory().getName()
                                    : "N/A"
                    )
                    .append("</td>");

            table.append("<td style='border:1px solid #ddd;padding:8px;'>")
                    .append(
                            income.getDate() != null
                                    ? income.getDate()
                                    : ""
                    )
                    .append("</td>");

            table.append("</tr>");
        }

        table.append("</table>");

        String body =
                "Hi " + profile.getFullName() +
                        ",<br><br>" +
                        "Here are your income details:<br><br>" +
                        table +
                        "<br><br>" +
                        "Best regards,<br>" +
                        "Money Manager Team";

        emailService.sendEmail(
                profile.getEmail(),
                "Your Income Details",
                body
        );
    }


    // Convert DTO → Entity
    private IncomeEntity toEntity(
            IncomeDTO incomeDTO,
            ProfileEntity profile,
            CategoryEntity category) {

        return IncomeEntity.builder()
                .name(incomeDTO.getName())
                .icon(incomeDTO.getIcon())
                .amount(incomeDTO.getAmount())

                // Use the provided date.
                // If no date is provided, use today's date.
                .date(incomeDTO.getDate() != null
                        ? incomeDTO.getDate()
                        : LocalDate.now())

                .profile(profile)
                .category(category)
                .build();
    }


    // Convert Entity → DTO
    private IncomeDTO toDTO(IncomeEntity entity) {

        return IncomeDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())

                .categoryId(
                        entity.getCategory() != null
                                ? entity.getCategory().getId()
                                : null
                )

                .categoryName(
                        entity.getCategory() != null
                                ? entity.getCategory().getName()
                                : "N/A"
                )

                .amount(entity.getAmount())

                // ⭐ Important: include date in API response
                .date(entity.getDate())

                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())

                .build();
    }
}