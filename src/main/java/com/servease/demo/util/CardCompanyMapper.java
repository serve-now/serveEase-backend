package com.servease.demo.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//토스페이먼츠 카드사 코드 : (https://docs.tosspayments.com/reference/codes#카드사-코드)

public final class CardCompanyMapper {

    private static final Map<String, String> CARD_COMPANY_MAP;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("3K", "기업비씨");
        map.put("46", "광주은행");
        map.put("71", "롯데카드");
        map.put("30", "한국산업은행");
        map.put("31", "BC카드");
        map.put("51", "삼성카드");
        map.put("38", "새마을금고");
        map.put("41", "신한카드");
        map.put("62", "신협");
        map.put("36", "씨티카드");
        map.put("33", "우리BC카드");
        map.put("W1", "우리카드");
        map.put("37", "우체국예금보험");
        map.put("39", "저축은행중앙회");
        map.put("35", "전북은행");
        map.put("42", "제주은행");
        map.put("15", "카카오뱅크");
        map.put("3A", "케이뱅크");
        map.put("24", "토스뱅크");
        map.put("21", "하나카드");
        map.put("61", "현대카드");
        map.put("11", "KB국민카드");
        map.put("91", "NH농협카드");
        map.put("34", "Sh수협은행");
        CARD_COMPANY_MAP = Collections.unmodifiableMap(map);
    }

    private CardCompanyMapper() {
    }

    public static String map(String issuerCode) {
        if (issuerCode == null) {
            return null;
        }

        String normalized = issuerCode.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        String key = normalized.toUpperCase(Locale.ROOT);
        return CARD_COMPANY_MAP.getOrDefault(key, normalized);
    }
}
