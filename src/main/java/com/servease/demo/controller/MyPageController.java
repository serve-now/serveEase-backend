package com.servease.demo.controller;

import com.servease.demo.dto.request.PasswordChangeRequest;
import com.servease.demo.dto.request.PhoneNumberUpdateRequest;
import com.servease.demo.dto.response.MyPageResponse;
import com.servease.demo.model.entity.User;
import com.servease.demo.service.MyPageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public MyPageResponse getProfile(@AuthenticationPrincipal User user) {
        return myPageService.getProfile(user.getId());
    }

    @PatchMapping("/password")
    public MyPageResponse changePassword(@AuthenticationPrincipal User user,
                                         @Valid @RequestBody PasswordChangeRequest request) {
        return myPageService.changePassword(user.getId(), request);
    }

    @PatchMapping("/phone-number")
    public MyPageResponse changePhoneNumber(@AuthenticationPrincipal User user,
                                            @Valid @RequestBody PhoneNumberUpdateRequest request) {
        return myPageService.changePhoneNumber(user.getId(), request);
    }
}
