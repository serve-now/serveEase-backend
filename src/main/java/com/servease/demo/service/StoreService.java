package com.servease.demo.service;

import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.model.entity.Store;
import com.servease.demo.model.entity.User;
import com.servease.demo.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional
    public Store createStore(String storeName, User owner) {
        Store newStore = Store.create(owner, storeName);
        return storeRepository.save(newStore);
    }

    public Store getStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND, "Store not found: " + storeId));
    }

    @Transactional
    public Store updateStoreName(Long storeId, Long ownerId, String storeName) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND, "Store not found: " + storeId));

        if (!store.getOwner().getId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.STORE_ACCESS_DENIED);
        }

        store.changeName(storeName);
        return store;
    }
}
