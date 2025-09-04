package com.servease.demo.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminRegisterRequest {
    private String username;
    private String password;
}
