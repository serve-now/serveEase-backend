package com.servease.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignUpRequest {

    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하로 입력해주세요.")
    @Pattern(regexp = "^[a-z0-9]+$", message = "아이디는 영문 소문자와 숫자만 사용할 수 있습니다.")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 72, message = "비밀번호는 8자 이상 72자 이하로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d\\S]*$", message = "비밀번호는 영문과 숫자를 필수로 포함해야 합니다.")
    private String password;

    @NotBlank(message = "전화번호를 입력해주세요.")
    @Size(min = 10, max = 11, message = "전화번호는 10자 또는 11자여야 합니다.")
    @Pattern(regexp = "^[0-9]+$", message = "전화번호는 숫자만 입력해주세요.")
    private String phoneNumber;

    @NotBlank(message = "사용자 이름을 입력해주세요.")
    @Size(max = 50, message = "사용자 이름은 50자 이하로 입력해주세요.")
    private String username;

    @NotBlank(message = "매장 이름을 입력해주세요.")
    @Size(max = 100, message = "매장 이름은 100자 이하로 입력해주세요.")
    private String storeName;

    @NotNull(message = "테이블 개수를 입력해주세요.")
    @Min(value = 1, message = "테이블은 최소 1개 이상이어야 합니다.")
    private Integer tableCount;
}
