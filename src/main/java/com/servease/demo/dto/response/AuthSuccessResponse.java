package com.servease.demo.dto.response;

import com.servease.demo.model.entity.Store;
import com.servease.demo.model.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class AuthSuccessResponse {

    private Long userId;
    private String username;
    private String token;
    private List<StoreInfo> stores;


    @Getter
    @Builder
    private static class StoreInfo {
        private Long storeId;
        private String storeName;

        static StoreInfo from(Store store) {
            return StoreInfo.builder()
                    .storeId(store.getId())
                    .storeName(store.getName())
                    .build();
        }
    }

    public static AuthSuccessResponse from(User user, String token) {
        List<StoreInfo> storeInfos = user.getStores().stream()
                .map(StoreInfo::from)
                .collect(Collectors.toList());

        return AuthSuccessResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .token(token)
                .stores(storeInfos)
                .build();
    }
}