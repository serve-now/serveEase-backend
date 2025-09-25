package com.servease.demo.dto.response;

import com.servease.demo.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSignupResponse {
    private Long userId;
    private String loginId;
    private String username;
    private String storeName;

    public static UserSignupResponse from(User user, String StoreName) {
        return new UserSignupResponse(
                user.getId(),
                user.getLoginId(),
                user.getUsername(),
                StoreName
        );
    }
}
