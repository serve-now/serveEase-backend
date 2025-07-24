package com.servease.demo.controller;

import com.servease.demo.dto.request.MenuCreateRequest;
import com.servease.demo.dto.request.MenuUpdateRequest;
import com.servease.demo.dto.response.MenuResponse;
import com.servease.demo.model.entity.Menu;
import com.servease.demo.model.entity.Order;
import com.servease.demo.service.MenuService;
import org.hibernate.boot.model.naming.ImplicitEntityNameSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping
    public ResponseEntity<MenuResponse> createMenu(@RequestBody MenuCreateRequest request) {
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
        Optional<MenuResponse> menuResponseOptional = menuService.getMenuById(menuId);
        return menuResponseOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //update
    @PutMapping("/{menuId}")
    public ResponseEntity<MenuResponse> updateMenu(@PathVariable Long menuId, @RequestBody MenuUpdateRequest request) {
        MenuResponse udpateMenuResponse = menuService.updateMenu(menuId, request);
        return ResponseEntity.ok(udpateMenuResponse);
    }

    //delete
    @DeleteMapping("/{menuId}")
    public ResponseEntity<MenuResponse> deleteMenu(@PathVariable Long menuId) {
        menuService.deleteMenu(menuId);
        return ResponseEntity.noContent().build();
    }

}
