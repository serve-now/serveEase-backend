package com.servease.demo.service;

import com.servease.demo.dto.request.PasswordChangeRequest;
import com.servease.demo.dto.request.PhoneNumberUpdateRequest;
import com.servease.demo.dto.response.MyPageResponse;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.model.entity.User;
import com.servease.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public MyPageResponse getProfile(Long userId) {
        User user = getUser(userId);
        return MyPageResponse.from(user);
    }

    @Transactional
    public MyPageResponse changePassword(Long userId, PasswordChangeRequest request) {
        User user = getUser(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        return MyPageResponse.from(user);
    }

    @Transactional
    public MyPageResponse changePhoneNumber(Long userId, PhoneNumberUpdateRequest request) {
        User user = getUser(userId);
        if (userRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), userId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE_NUMBER);
        }

        user.changePhoneNumber(request.getPhoneNumber());
        return MyPageResponse.from(user);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
