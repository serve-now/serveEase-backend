package com.servease.demo.repository;

import com.servease.demo.model.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    Optional<Menu> findByStoreIdAndName(Long storeId, String name);

    List<Menu> findAllByStoreId(Long storeId);

    List<Menu> findByStoreIdAndAvailable(Long storeId, boolean available);

    Optional<Menu> findByName(String name);

    List<Menu> findByavailableIsTrue();
    boolean existsByCategoryId(Long categoryId);

}
