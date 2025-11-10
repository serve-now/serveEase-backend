package com.servease.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PhoneNumberUpdateRequest {

    @NotBlank(message = "전화번호를 입력해주세요.")
    @Size(min = 10, max = 11, message = "전화번호는 10자 또는 11자여야 합니다.")
    @Pattern(regexp = "^[0-9]+$", message = "전화번호는 숫자만 입력해주세요.")
    private String phoneNumber;
}
