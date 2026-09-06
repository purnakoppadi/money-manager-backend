package com.purnakoppadi.moneymanager.service;


import com.purnakoppadi.moneymanager.dto.AuthDto;
import com.purnakoppadi.moneymanager.dto.CategoryDTO;
import com.purnakoppadi.moneymanager.entity.CategoryEntity;
import com.purnakoppadi.moneymanager.entity.ProfileEntity;
import com.purnakoppadi.moneymanager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;


    public CategoryDTO saveCategory(CategoryDTO categoryDTO)
    {
        ProfileEntity profile=profileService.getCurrentProfile();
        if(categoryRepository.existsByNameAndProfileId(categoryDTO.getName(),profile.getId()))
        {
            throw new RuntimeException("Category with this name already exists");
        }
        CategoryEntity newCategory=toEntity(categoryDTO,profile);
        newCategory=categoryRepository.save(newCategory);
        return toDTO(newCategory);

    }

    private CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profile)
    {
        return CategoryEntity.builder()
                .name(categoryDTO.getName())
                .icon(categoryDTO.getIcon())
                .profile(profile)
                .type(categoryDTO.getType())
                .build();
    }

    private CategoryDTO toDTO(CategoryEntity categoryEntity)
    {
        return CategoryDTO.builder()
                .id(categoryEntity.getId())
                .profileId(categoryEntity.getProfile()!=null ? categoryEntity.getProfile().getId() : null)
                .name(categoryEntity.getName())
                .icon(categoryEntity.getIcon())
                .createdAt(categoryEntity.getCreatedAt())
                .updatedAt(categoryEntity.getUpdatedAt())
                .type(categoryEntity.getType())
                .build();


    }

    public List<CategoryDTO> getCategoriesForCurrentUser()
    {
        ProfileEntity profile=profileService.getCurrentProfile();
        List<CategoryEntity> categoryEntities=categoryRepository.findByProfileId(profile.getId());
        return categoryEntities.stream().map(this::toDTO).toList();
    }

    public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String type)
    {
        ProfileEntity profile=profileService.getCurrentProfile();
        List<CategoryEntity> categoryEntities=categoryRepository.findByTypeAndProfileId(type,profile.getId());
        return categoryEntities.stream().map(this::toDTO).toList();
    }

    public CategoryDTO updateCategory(Long categoryId,CategoryDTO categoryDTO)
    {
        ProfileEntity profile=profileService.getCurrentProfile();
        CategoryEntity existingCategory=categoryRepository.findByIdAndProfileId(categoryId,profile.getId())
                .orElseThrow(()-> new RuntimeException("Category not found"));
        existingCategory.setName(categoryDTO.getName());
        existingCategory.setIcon(categoryDTO.getIcon());
        existingCategory=categoryRepository.save(existingCategory);
        return toDTO(existingCategory);



    }

}
