package com.servease.demo.service;

import com.servease.demo.dto.request.MenuCreateRequest;
import com.servease.demo.dto.response.MenuResponse;
import com.servease.demo.dto.request.MenuUpdateRequest;
import com.servease.demo.model.entity.Menu;
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

    @Autowired
    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    //TODO 에러코드 전체 500으로 내려감
    @Transactional
    public MenuResponse createMenu(MenuCreateRequest request) {
        if (menuRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Menu with name" + request.getName() + "already exists.");
        }

        Menu newMenu = Menu.builder()
                .name(request.getName())
                .price(request.getPrice())
                .category(request.getCategory())
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

        //여기서 updateMenu인데, 이미 menu 이름이 있는지 찾는 경우에도 findById ?
        if (!menu.getName().equals(request.getName()) && menuRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Menu with name" + request.getName() + "already exists");
        }

        menu.setName(request.getName());
        menu.setPrice(request.getPrice());
        menu.setCategory(request.getCategory());
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
