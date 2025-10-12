package com.servease.demo.controller;

import com.servease.demo.global.exception.BusinessException;
import com.servease.demo.global.exception.ErrorCode;
import com.servease.demo.global.exception.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //우리가 정의한 비즈니스 예외시 여기로
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        final ErrorCode errorCode = e.getErrorCode();
        final ErrorResponse response = ErrorResponse.of(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                e.getDetail()
        );
        return new ResponseEntity<>(response, errorCode.getStatus());
    }


    //DTO @Vaild 검증 실패시 여기로
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentValid(MethodArgumentNotValidException exception) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        List<ErrorResponse.FieldErrorItem> items = exception.getBindingResult().getFieldErrors().stream()
                .map(fe -> ErrorResponse.FieldErrorItem.builder()
                        .field(fe.getField())
                        .rejectedValue(fe.getRejectedValue())
                        .message(fe.getDefaultMessage())
                        .build())
                .toList();

        ErrorResponse body = ErrorResponse.of(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),    //title: "입력 값이 올바르지 않습니다"
                "Request Validation failed",
                items //MethodArgumentNotValidException은 어떤 필드가 에러났는지 FieldError 리스트를 갖고 있음
                //이걸 받아서 items 에 담아 전달
        );

        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception ex) {
        // log.error("Unhandled excepion", ex); // 운영에선 로그 남겨야함
        ErrorCode ec = ErrorCode.INTERNAL_SERVER_ERROR;

        ErrorResponse body = ErrorResponse.of(
                ec.getStatus().value(),
                ec.getCode(),
                ec.getMessage(),            // title: "서버 내부 오류가 발생했습니다."
                ex.getMessage()             // detail: 런타임 메시지
        );
        return ResponseEntity.status(ec.getStatus()).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredential(BadCredentialsException e) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        final ErrorResponse response = ErrorResponse.of(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                ""
        );

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

}
