package com.servease.demo.repository;

import com.servease.demo.model.entity.RestaurantTable;
import com.servease.demo.model.entity.Store;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    Page<RestaurantTable> findAll(Pageable pageable);

    Optional<RestaurantTable> findByStoreAndTableNumber(Store store, Integer tableNumber);

    Optional<RestaurantTable> findByTableNumber(Integer tableNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM RestaurantTable t WHERE t.id = :id")
    Optional<RestaurantTable> findByIdWithLock(@Param("id") Long id);

}
