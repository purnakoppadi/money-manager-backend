package com.purnakoppadi.moneymanager.controller;


import com.purnakoppadi.moneymanager.dto.CategoryDTO;
import com.purnakoppadi.moneymanager.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;


    @PostMapping
    public ResponseEntity<CategoryDTO> saveCategory(@RequestBody CategoryDTO categoryDTO)
    {
        CategoryDTO savedCategory=categoryService.saveCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
    }


    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getCategories()
    {
        List<CategoryDTO> categories=categoryService.getCategoriesForCurrentUser();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{type}")
    public ResponseEntity<List<CategoryDTO>> getCategoriesByType(@PathVariable String type)
    {
        List<CategoryDTO> categoryDTOS=categoryService.getCategoriesByTypeForCurrentUser(type);
        return ResponseEntity.ok(categoryDTOS);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long categoryId,@RequestBody CategoryDTO categoryDTO)
    {
        CategoryDTO categoryDTO1=categoryService.updateCategory(categoryId,categoryDTO);
        return ResponseEntity.ok(categoryDTO1);
    }
}
