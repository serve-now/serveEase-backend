package com.servease.demo.dto.response;

import com.servease.demo.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRegisterResponse {
    private Long userId;
    private String loginId;
    private String username;
    private String storeName;

    public static UserRegisterResponse from(User user, String StoreName) {
        return new UserRegisterResponse(
                user.getId(),
                user.getLoginId(),
                user.getUsername(),
                StoreName
        );
    }
}
