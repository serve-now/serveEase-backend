package com.servease.demo.controller;

import com.servease.demo.dto.request.MenuCreateRequest;
import com.servease.demo.dto.request.MenuUpdateRequest;
import com.servease.demo.dto.response.MenuResponse;
import com.servease.demo.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores/{storeId}/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping
    public ResponseEntity<MenuResponse> createMenu(@PathVariable Long storeId, @Valid @RequestBody MenuCreateRequest request) {
        MenuResponse createdMenu = menuService.createMenu(storeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMenu);
    }

    @GetMapping
    public ResponseEntity<List<MenuResponse>> getAllMenusByStore(@PathVariable Long storeId) {
        List<MenuResponse> menuResponses = menuService.getAllMenusByStore(storeId);
        return ResponseEntity.ok(menuResponses);
    }


    @GetMapping("/available")
    public ResponseEntity<List<MenuResponse>> getAvailableMenusByStore(@PathVariable Long storeId) {
        List<MenuResponse> menuResponses = menuService.getAvailableMenusByStore(storeId);
        return ResponseEntity.ok(menuResponses);
    }


    @GetMapping("/{menuId}")
    public ResponseEntity<MenuResponse> getMenuById(
            @PathVariable Long storeId,
            @PathVariable Long menuId) {
        MenuResponse menuResponse = menuService.getMenuById(storeId, menuId);
        return ResponseEntity.ok(menuResponse);
    }


    @PutMapping("/{menuId}")
    public ResponseEntity<MenuResponse> updateMenu(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @Valid @RequestBody MenuUpdateRequest request) {
        MenuResponse updatedMenuResponse = menuService.updateMenu(storeId, menuId, request);
        return ResponseEntity.ok(updatedMenuResponse);
    }


    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> deleteMenu(
            @PathVariable Long storeId,
            @PathVariable Long menuId) {
        menuService.deleteMenu(storeId, menuId);
        return ResponseEntity.noContent().build();
    }

}
