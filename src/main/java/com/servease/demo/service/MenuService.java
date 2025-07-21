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

    @Transactional

    public Menu updateMenu(Long id, String name, Integer price, String category, Boolean isAvailable){
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found with ID : " + id));

        //여기서 updateMenu인데, 이미 menu 이름이 있는지 찾는 경우에도 findById ?
        if (!menu.getName().equals(name) && menuRepository.findByName(name).isPresent()){
            throw new IllegalArgumentException("Menu with name" + name + "already exists");
        }

        menu.setName(name);
        menu.setPrice(price);
        menu.setCategory(category);
        menu.setIsAvailable(isAvailable);

        return menuRepository.save(menu);
    }

    @Transactional
    public void deleteMenu(Long id){
        if (!menuRepository.existsById(id)) {
            throw new IllegalArgumentException("Menu not found with ID" + id);
        }
        menuRepository.deleteById(id);
    }

}
