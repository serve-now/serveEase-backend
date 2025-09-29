package com.servease.demo.service;

import com.servease.demo.dto.request.CategoryRequest;
import com.servease.demo.dto.response.CategoryResponse;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.model.entity.Category;
import com.servease.demo.model.entity.Store;
import com.servease.demo.repository.CategoryRepository;
import com.servease.demo.repository.MenuRepository;
import com.servease.demo.repository.StoreRepository;
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
    private final StoreRepository storeRepository;


    @Transactional
    public CategoryResponse createCategory(Long storeId, CategoryRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        if (categoryRepository.findByStoreIdAndName(storeId, request.getName()).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_CATEGORY_NAME, "Category name '" + request.getName() + "' already exists.");
        }

        Category category = Category.builder()
                .store(store)
                .name(request.getName())
                .build();

        Category savedCategory = categoryRepository.save(category);
        return CategoryResponse.from(savedCategory);
    }


    public List<CategoryResponse> getAllCategoriesByStore(Long storeId) {
        return categoryRepository.findAllByStoreIdOrderByIdAsc(storeId).stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse updateCategory(Long storeId, Long categoryId, CategoryRequest request) {
        Category category = findCategoryAndVerifyOwnership(storeId, categoryId);

        if (request.getName() != null && !request.getName().equals(category.getName())) {
            categoryRepository.findByStoreIdAndName(storeId, request.getName()).ifPresent(existingCategory -> {
                if (!existingCategory.getId().equals(categoryId)) {
                    throw new BusinessException(ErrorCode.DUPLICATE_CATEGORY_NAME, "Category name '" + request.getName() + "' already exists in this store.");
                }
            });
        }

        category.updateName(request.getName());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void deleteCategory(Long storeId, Long categoryId) {
        Category category = findCategoryAndVerifyOwnership(storeId, categoryId);

        if (menuRepository.existsByCategoryId(categoryId)) {
            throw new BusinessException(ErrorCode.CATEGORY_IN_USE, "Category id=" + categoryId + " is still referenced by existing menus");
        }
        categoryRepository.deleteById(categoryId);
    }

    private Category findCategoryAndVerifyOwnership(Long storeId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, "Category not found with ID: " + categoryId));

        if (!category.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "This category does not belong to the specified store.");
        }
        return category;
    }
}
