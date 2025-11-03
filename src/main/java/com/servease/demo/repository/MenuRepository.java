package com.servease.demo.repository;

import com.servease.demo.model.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    boolean existsByCategoryIdAndDeletedAtIsNull(Long categoryId);

    List<Menu> findAllByIdInAndAvailableIsTrueAndDeletedAtIsNull(List<Long> menuIds);

    Optional<Menu> findByIdAndDeletedAtIsNull(Long id);

    Optional<Menu> findByStoreIdAndNameAndDeletedAtIsNull(Long storeId, String name);

    List<Menu> findAllByStoreIdAndDeletedAtIsNull(Long storeId);

    List<Menu> findByStoreIdAndAvailableAndDeletedAtIsNull(Long storeId, boolean available);
}
