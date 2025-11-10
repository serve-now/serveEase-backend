package com.servease.demo.repository;

import com.servease.demo.model.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByIdAndOwnerId(Long storeId, Long ownerId);
}
