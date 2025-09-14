package com.servease.demo.service;

import com.servease.demo.dto.request.CategoryRequest;
import com.servease.demo.dto.response.CategoryResponse;
import com.servease.demo.model.entity.Category;
import com.servease.demo.repository.CategoryRepository;
import com.servease.demo.repository.MenuRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;


    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        categoryRepository.findByName(request.getName()).ifPresent(category -> {
            throw new IllegalArgumentException("Category name already exists with ID: " + request.getName());
        });

        Category category = request.toEntity();
        Category savedCategory = categoryRepository.save(category);
        return CategoryResponse.from(savedCategory);
    }


    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByIdAsc().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다. ID: " + categoryId));

        if (request.getName() != null && !request.getName().equals(category.getName())) {
            categoryRepository.findByName(request.getName()).ifPresent(existingCategory -> {
                if (!existingCategory.getId().equals(categoryId)) {
                    throw new IllegalArgumentException("이미 존재하는 카테고리 이름입니다: " + request.getName());
                }
            });
        }

        category.updateName(request.getName());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category categoryToDelete = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));

        if (menuRepository.existsByCategoryId(categoryId)) {
            throw new IllegalStateException("Cannot delete category because it is in use by one or more menus.");
        }
        categoryRepository.deleteById(categoryId);
    }
}
