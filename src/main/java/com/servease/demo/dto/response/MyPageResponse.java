package com.servease.demo.dto.response;

import com.servease.demo.model.entity.Store;
import com.servease.demo.model.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class MyPageResponse {

    private Long userId;
    private String username;
    private String phoneNumber;
    private List<StoreInfo> stores;

    @Getter
    @Builder
    public static class StoreInfo {
        private Long storeId;
        private String storeName;

        public static StoreInfo from(Store store) {
            return StoreInfo.builder()
                    .storeId(store.getId())
                    .storeName(store.getName())
                    .build();
        }
    }

    public static MyPageResponse from(User user) {
        List<StoreInfo> storeInfos = user.getStores().stream()
                .map(StoreInfo::from)
                .collect(Collectors.toList());

        return MyPageResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .stores(storeInfos)
                .build();
    }
}
