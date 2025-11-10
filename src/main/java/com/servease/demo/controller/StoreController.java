package com.servease.demo.controller;

import com.servease.demo.dto.request.StoreCreateRequest;
import com.servease.demo.dto.request.StoreNameUpdateRequest;
import com.servease.demo.dto.response.StoreResponse;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.model.entity.Store;
import com.servease.demo.model.entity.User;
import com.servease.demo.service.StoreService;
import com.servease.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;
    private final UserService userService;


    @PostMapping
    public ResponseEntity<StoreResponse> createStore(@RequestBody StoreCreateRequest request) {
        User owner = userService.findUserById(request.getOwnerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Store newStore = storeService.createStore(request.getStoreName(), owner);

        return ResponseEntity.status(HttpStatus.CREATED).body(StoreResponse.from(newStore));
    }

    @PatchMapping("/{storeId}/name")
    public StoreResponse updateStoreName(@PathVariable Long storeId,
                                         @AuthenticationPrincipal User user,
                                         @Valid @RequestBody StoreNameUpdateRequest request) {
        Store store = storeService.updateStoreName(storeId, user.getId(), request.getStoreName());
        return StoreResponse.from(store);
    }

}
