package com.servease.demo.controller;

import com.servease.demo.dto.request.UserLoginRequest;
import com.servease.demo.dto.request.UserSignUpRequest;
import com.servease.demo.dto.response.UserLoginResponse;
import com.servease.demo.dto.response.UserSignupResponse;
import com.servease.demo.service.UserService;
import com.servease.demo.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponse> signUp(@Valid @RequestBody UserSignUpRequest request) {
        UserSignupResponse response = userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
