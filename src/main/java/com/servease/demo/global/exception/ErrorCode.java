package com.servease.demo.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "E001", "입력 값이 올바르지 않습니다."),
    MENU_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "E009", "현재 주문할 수 없는 메뉴입니다."),

    // 404 Not Found
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "E002", "메뉴를 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "E003", "카테고리를 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "E004", "주문을 찾을 수 없습니다."),
    TABLE_NOT_FOUND(HttpStatus.NOT_FOUND, "E010", "테이블을 찾을 수 없습니다."),
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "E012", "해당 주문 항목을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"E016","해당 유저를 찾을 수 없습니다."),

    // 409 Conflict
    DUPLICATE_MENU_NAME(HttpStatus.CONFLICT, "E005", "이미 존재하는 메뉴 이름입니다."),
    DUPLICATE_CATEGORY_NAME(HttpStatus.CONFLICT, "E006", "이미 존재하는 카테고리 이름입니다."),
    CATEGORY_IN_USE(HttpStatus.CONFLICT, "E007", "해당 카테고리를 사용하는 메뉴가 있어 삭제할 수 없습니다."),
    ORDER_STATUS_NOT_VALID(HttpStatus.CONFLICT, "E008", "완료되거나 취소된 주문입니다."),
    DUPLICATE_TABLE_NUMBER(HttpStatus.CONFLICT, "E011", "이미 존재하는 테이블 번호입니다."),
    ACTIVE_ORDER_EXISTS(HttpStatus.CONFLICT, "E013", "이미 진행 중인 주문이 테이블에 존재합니다."),
    ORDER_ALREADY_PAID(HttpStatus.CONFLICT, "E014", "이미 결제가 완료된 주문입니다."),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "E015", "이미 사용 중인 아이디입니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E999", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}