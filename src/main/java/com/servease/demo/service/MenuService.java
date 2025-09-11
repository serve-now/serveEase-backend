package com.servease.demo.service;

import com.servease.demo.dto.request.MenuCreateRequest;
import com.servease.demo.dto.response.MenuResponse;
import com.servease.demo.dto.request.MenuUpdateRequest;
import com.servease.demo.model.entity.Category;
import com.servease.demo.model.entity.Menu;
import com.servease.demo.repository.CategoryRepository;
import com.servease.demo.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public MenuService(MenuRepository menuRepository, CategoryRepository categoryRepository) {
        this.menuRepository = menuRepository;
        this.categoryRepository = categoryRepository;
    }

    //TODO 에러코드 전체 500으로 내려감
    @Transactional
    public MenuResponse createMenu(MenuCreateRequest request) {
        if (menuRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Menu with name" + request.getName() + "already exists.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(()-> new IllegalArgumentException("Category not found with ID: " + request.getCategoryId()));

        Menu newMenu = Menu.builder()
                .name(request.getName())
                .price(request.getPrice())
                .category(category)
                .available(request.isAvailable())
                .build();

        Menu savedMenu = menuRepository.save(newMenu);
        return MenuResponse.fromEntity(savedMenu);
    }

    public List<MenuResponse> getAllMenus() {
        List<Menu> menus = menuRepository.findAll();
        return menus.stream()
                .map(MenuResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MenuResponse> getAvailableMenus() {
        List<Menu> menus = menuRepository.findByavailableIsTrue();
        return menus.stream()
                .map(MenuResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<MenuResponse> getMenuById(Long id) {
        Optional<Menu> menuOptional = menuRepository.findById(id);
        return menuOptional.map(MenuResponse::fromEntity);
    }

//    @Transactional
//    public MenuResponse updateMenuAvailability(Long menuId, MenuUpdateRequest request) {
//        Menu menu = menuRepository.findById(menuId)
//                .orElseThrow(() -> new IllegalArgumentException("Menu not found with ID: " + menuId));
//        menu.setAvailable(request.setAvailable());
//        Menu updatedMenu = menuRepository.save(menu);
//        return MenuResponse.fromEntity(updatedMenu);
//    }

    @Transactional
    public MenuResponse updateMenu(Long menuId, MenuUpdateRequest request) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found with ID : " + menuId));

        if (request.getName() != null && !request.getName().equals(menu.getName())) {
            menuRepository.findByName(request.getName()).ifPresent(m -> {
                throw new IllegalArgumentException("Menu with name '" + request.getName() + "' already exists.");
            });
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + request.getCategoryId()));


        menu.setName(request.getName());
        menu.setPrice(request.getPrice());
        menu.setCategory(category);
        menu.setAvailable(request.isAvailable());

        Menu updatedMenu = menuRepository.save(menu);
        return MenuResponse.fromEntity(updatedMenu);
    }

    @Transactional
    public void deleteMenu(Long menuId) {
        if (!menuRepository.existsById(menuId)) {
            throw new IllegalArgumentException("Menu not found with ID " + menuId);
        }
        menuRepository.deleteById(menuId);
    }

}
