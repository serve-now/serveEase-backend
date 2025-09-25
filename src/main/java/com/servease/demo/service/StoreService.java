package com.servease.demo.service;

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
}
