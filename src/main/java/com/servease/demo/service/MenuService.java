package com.servease.demo.service;

import com.servease.demo.model.entity.Menu;
import com.servease.demo.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;

    @Autowired
    public MenuService(MenuRepository menuRepository){
        this.menuRepository = menuRepository;
    }

    @Transactional
    public Menu createMenu(String name, Integer price, String category, Boolean isAvailable) {
        if(menuRepository.findByName(name).isPresent()){
            throw new IllegalArgumentException("Menu with name" + name +"already exists.");
        }

        Menu newMenu = Menu.builder()
                .name(name)
                .price(price)
                .category(category)
                .isAvailable(isAvailable)
                .build();
        return menuRepository.save(newMenu);
    }

    public List<Menu> getAllMenus(){
        return menuRepository.findAll();
    }

    public List<Menu> getAvailableMenus(){
        return menuRepository.findByIsAvailableTrue();
    }

    public Optional<Menu> getMenuById(Long id){
        return menuRepository.findById(id);
    }

    @Transactional
    public Menu updateMenuAvailability(Long id, Boolean isAvailable){
        Menu menu = menuRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("Menu not found with ID: " + id));
        menu.setIsAvailable(isAvailable);
        return menuRepository.save(menu);
    }
}
