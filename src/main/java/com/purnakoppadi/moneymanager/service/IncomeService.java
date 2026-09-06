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

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;


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