package com.servease.demo.controller;

import com.servease.demo.dto.request.UserLoginRequest;
import com.servease.demo.dto.request.UserSignUpRequest;
import com.servease.demo.dto.response.AuthSuccessResponse;
import com.servease.demo.model.entity.User;
import com.servease.demo.service.UserService;
import com.servease.demo.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthSuccessResponse signUp(@Valid @RequestBody UserSignUpRequest request) {
        return userService.signUp(request);
    }

    @PostMapping("/login")
    public AuthSuccessResponse login(@RequestBody UserLoginRequest request) {
        // Spring Security(authenticationManager) 를 통해 인증 시도
        //TODO: 다른 가게일 경우도 auth 확인해야함
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLoginId(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user.getLoginId());

        return AuthSuccessResponse.from(user, token);
    }
}