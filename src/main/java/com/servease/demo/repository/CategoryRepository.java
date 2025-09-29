package com.servease.demo.repository;

import com.servease.demo.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByStoreIdAndName(Long storeId, String name);

    List<Category> findAllByStoreIdOrderByIdAsc(Long storeId);
}
