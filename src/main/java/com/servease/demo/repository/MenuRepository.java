package com.servease.demo.repository;

import com.servease.demo.model.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu,Long> {

    Optional<Menu> findByName(String name);

    List<Menu> findByIsAvailableTrue();

}
