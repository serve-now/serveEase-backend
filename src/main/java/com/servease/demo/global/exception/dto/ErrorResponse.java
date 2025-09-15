package com.servease.demo.global.exception.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Getter
@Builder
public class ErrorResponse {
    private final int status; //HTTP 응답
    private final String errorCode; //비즈니스 에러코드
    private final String title;
    private final String detail; //error.message
    private final String timestamp;
    private final List<FieldErrorItem> errors; // DTO검증 실패시 사용


    public static ErrorResponse of(int status, String code, String title, String detail) {
        return ErrorResponse.builder()
                .status(status)
                .errorCode(code)
                .title(title)
                .detail(detail)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC).toString())
                .build();
    }

    public static ErrorResponse of(int status, String code, String title, String detail,
                                   List<FieldErrorItem> errors) {
        return ErrorResponse.builder()
                .status(status)
                .errorCode(code)
                .title(title)
                .detail(detail)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC).toString())
                .errors(errors)
                .build();
    }


    @Getter
    @Builder
    public static class FieldErrorItem {
        private final String field;
        private final Object rejectedValue;
        private final String message;
    }

}
