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

    @Query("""
    select distinct t
    from RestaurantTable t
    left join fetch t.orders o
    where t.id in :ids
      and (o is null or o.status in :activeStatuses)
    order by t.tableNumber asc
""")
    List<RestaurantTable> findAllWithActiveOrdersByIds(
            @Param("ids") List<Long> ids,
            @Param("activeStatuses") List<com.servease.demo.model.enums.OrderStatus> activeStatuses
    );

}
