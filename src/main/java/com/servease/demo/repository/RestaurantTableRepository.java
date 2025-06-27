package com.servease.demo.repository;

import com.servease.demo.model.entity.RestaurantTable;
import com.servease.demo.model.enums.RestaurantTableStatus;
import com.servease.demo.service.RestaurantTableService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    Optional<RestaurantTable> findByTableNumber(Integer tableNumber);

    List<RestaurantTable> findByStatus(RestaurantTableStatus status);
}
