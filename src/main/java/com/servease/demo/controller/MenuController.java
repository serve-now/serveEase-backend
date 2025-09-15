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
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping
    public ResponseEntity<MenuResponse> createMenu(@Valid @RequestBody MenuCreateRequest request) {
        MenuResponse menuResponse = menuService.createMenu(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(menuResponse);
    }

    @GetMapping
    public ResponseEntity<List<MenuResponse>> getAllMenus() {
        List<MenuResponse> menuResponses = menuService.getAllMenus();
        return ResponseEntity.ok(menuResponses);
    }

    //get available
    @GetMapping("/available")
    public ResponseEntity<List<MenuResponse>> getAvailableMenus() {
        List<MenuResponse> menuResponses = menuService.getAvailableMenus();
        return ResponseEntity.ok(menuResponses);
    }


    //get id
    @GetMapping("/{menuId}")
    public ResponseEntity<MenuResponse> getMenuById(@PathVariable Long menuId) {
        // [수정] 서비스에서 바로 응답 DTO를 받으므로 코드가 간결해집니다.
        MenuResponse menuResponse = menuService.getMenuById(menuId);
        return ResponseEntity.ok(menuResponse);
    }

    @PutMapping("/{menuId}")
    public ResponseEntity<MenuResponse> updateMenu(@PathVariable Long menuId, @Valid @RequestBody MenuUpdateRequest request) {
        MenuResponse udpateMenuResponse = menuService.updateMenu(menuId, request);
        return ResponseEntity.ok(udpateMenuResponse);
    }

    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long menuId) {
        menuService.deleteMenu(menuId);
        return ResponseEntity.noContent().build();
    }

}
