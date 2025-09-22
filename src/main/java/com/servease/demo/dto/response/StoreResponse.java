package com.servease.demo.dto.response;

import com.servease.demo.model.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoreResponse {
    private Long storeId;
    private String storeName;
    private String ownerName;

    public static StoreResponse from(Store store) {
        return new StoreResponse(
                store.getId(),
                store.getName(),
                store.getOwner().getUsername()
        );
    }
}