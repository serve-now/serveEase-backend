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
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
    public ResponseEntity<AuthSuccessResponse> signUp(@Valid @RequestBody UserSignUpRequest request) {
        AuthSuccessResponse response = userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthSuccessResponse> login(@RequestBody UserLoginRequest request) {
        try {
            // Spring Security(authenticationManager) 를 통해 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLoginId(), request.getPassword())
            );

            User user = (User) authentication.getPrincipal();

            String token = jwtUtil.generateToken(user.getLoginId());

            return ResponseEntity.ok(AuthSuccessResponse.from(user, token));
         }catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}