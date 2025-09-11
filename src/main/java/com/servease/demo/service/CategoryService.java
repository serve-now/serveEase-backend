package com.servease.demo.service;

import com.servease.demo.dto.request.CategoryRequest;
import com.servease.demo.dto.response.CategoryResponse;
import com.servease.demo.model.entity.Category;
import com.servease.demo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

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
}
