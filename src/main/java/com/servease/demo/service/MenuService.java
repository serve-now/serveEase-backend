package com.servease.demo.service;

import com.servease.demo.dto.request.MenuCreateRequest;
import com.servease.demo.dto.response.MenuResponse;
import com.servease.demo.dto.request.MenuUpdateRequest;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.model.entity.Category;
import com.servease.demo.model.entity.Menu;
import com.servease.demo.model.entity.Store;
import com.servease.demo.model.enums.OrderStatus;
import com.servease.demo.repository.CategoryRepository;
import com.servease.demo.repository.MenuRepository;
import com.servease.demo.repository.OrderItemRepository;
import com.servease.demo.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MenuService {

    private static final List<OrderStatus> ACTIVE_ORDER_STATUSES = List.of(
            OrderStatus.ORDERED,
            OrderStatus.SERVED,
            OrderStatus.PARTIALLY_PAID
    );

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;


    @Transactional
    public MenuResponse createMenu(Long storeId, MenuCreateRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        if (!category.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "This category does not belong to the specified store.");
        }

        if (menuRepository.findByStoreIdAndNameAndDeletedAtIsNull(storeId, request.getName()).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_MENU_NAME, "Menu name '" + request.getName() + "' already exists in this store.");
        }

        Menu newMenu = Menu.builder()
                .store(store)
                .name(request.getName())
                .price(request.getPrice())
                .category(category)
                .available(request.isAvailable())
                .build();

        Menu savedMenu = menuRepository.save(newMenu);
        return MenuResponse.fromEntity(savedMenu);
    }


    public List<MenuResponse> getAllMenusByStore(Long storeId) {
        return menuRepository.findAllByStoreIdAndDeletedAtIsNull(storeId).stream()
                .map(MenuResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MenuResponse> getAvailableMenusByStore(Long storeId) {
        return menuRepository.findByStoreIdAndAvailableAndDeletedAtIsNull(storeId, true).stream()
                .map(MenuResponse::fromEntity)
                .collect(Collectors.toList());
    }
    public MenuResponse getMenuById(Long storeId, Long menuId) {
        Menu menu = findMenuAndVerifyOwnership(storeId, menuId);
        return MenuResponse.fromEntity(menu);
    }


    @Transactional
    public MenuResponse updateMenu(Long storeId, Long menuId, MenuUpdateRequest request) {
        Menu menu = findMenuAndVerifyOwnership(storeId, menuId);

        if (!request.getCategoryId().equals(menu.getCategory().getId())) {
            Category newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, "Category id=" + request.getCategoryId() + " not found"));
            menu.updateMenu(request.getName(), request.getPrice(), newCategory, request.isAvailable());
        } else {
            menu.updateMenu(request.getName(), request.getPrice(), menu.getCategory(), request.isAvailable());
        }
        return MenuResponse.fromEntity(menu);
    }

    @Transactional
    public void deleteMenu(Long storeId, Long menuId) {
        Menu menu = findMenuAndVerifyOwnership(storeId, menuId);

        if (orderItemRepository.existsByMenuIdAndOrder_StatusIn(menuId, ACTIVE_ORDER_STATUSES)) {
            throw new BusinessException(ErrorCode.MENU_IN_USE,
                    "Menu id=" + menuId + " is linked to active orders and cannot be deleted.");
        }

        menuRepository.delete(menu);
    }

    private Menu findMenuAndVerifyOwnership(Long storeId, Long menuId) {
        Menu menu = menuRepository.findByIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND, "Menu not found with ID: " + menuId));

        if (!menu.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "This menu does not belong to the specified store.");
        }
        return menu;
    }

}
