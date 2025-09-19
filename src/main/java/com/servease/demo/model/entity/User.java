package com.servease.demo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity implements UserDetails {
    //implements UserDetails: Spring Security가 이해할 수 있는 UserDetails 규격입니다 라고 선언

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String username;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder
    public User(String loginId, String username, String password, String phoneNumber) {
        this.loginId = loginId;
        this.password = password;
        this.username = username;
        this.phoneNumber = phoneNumber;

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //SpringSecurity: 사용자의 권한 목록은 무엇인가요? -> 현재 권한 없음 (role 이 없음)
        return List.of();
    }

    @Override
    public String getPassword() {
        //SpringSecurity: 사용자의 비밀번호는 무엇인가요?
        return this.password;
    }

    @Override
    public String getUsername() {
        //SpringSecurity: 사용자를 식별할 고유 ID는 무엇인가요?
        return this.loginId;
    }

    //계정 상태 관리
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }


}
