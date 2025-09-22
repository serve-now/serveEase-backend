package com.servease.demo.service;

import com.servease.demo.dto.request.UserSignUpRequest;
import com.servease.demo.dto.response.UserRegisterResponse;
import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.model.entity.Store;
import com.servease.demo.model.entity.User;
import com.servease.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; //SpringSecurity 에 bean으로 등록되어있음
    private final RestaurantTableService restaurantTableService;
    private final StoreService storeService;


    @Override
    public UserDetails loadUserByUsername (String loginId) throws UsernameNotFoundException { //loadUserByUsername : UserDetailsService의 SpringSecurity 인터페이스로 부터 옴
        return userRepository.findByLoginId(loginId)
                .orElseThrow(()-> new UsernameNotFoundException(("User not found with loginID: " + loginId)));

    }

    @Transactional
    public UserRegisterResponse signUp(UserSignUpRequest request) {
        userRepository.findByLoginId(request.getLoginId()).ifPresent(user -> {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        });


        User newUser = User.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .phoneNumber(request.getPhoneNumber())
                .build();


        Store newStore = storeService.createStore(request.getStoreName(), newUser);

        for (int i = 1; i <= request.getTableCount(); i++) {
            restaurantTableService.createTable(i, newStore);
        }
        userRepository.save(newUser);
        return UserRegisterResponse.from(newUser, newStore.getName());
    }

}
