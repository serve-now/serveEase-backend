package com.servease.demo.repository;

import com.servease.demo.model.entity.RestaurantTable;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    Page<RestaurantTable> findAllByStoreId(Long storeId, Pageable pageable);

    List<RestaurantTable> findAllByStoreId(Long storeId);

    Optional<RestaurantTable> findByStoreIdAndTableNumber(Long storeId, Integer tableNumber);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM RestaurantTable t WHERE t.id = :id")
    Optional<RestaurantTable> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT DISTINCT t FROM RestaurantTable t " +
            "LEFT JOIN FETCH t.orders o " +
            //"LEFT JOIN FETCH o.orderItems oi " +
            "WHERE t.id IN :ids " +
            "AND (o.status = 'ORDERED' OR o.status = 'SERVED' OR o IS NULL) " +
            "ORDER BY t.tableNumber ASC")
    List<RestaurantTable> findAllWithActiveOrdersByIds(@Param("ids") List<Long> ids);

}
