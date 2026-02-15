package com.booktalk_be.domain.gathering.util;

import java.math.BigInteger;

/**
 * 데이터베이스 조회 결과의 타입 변환을 위한 유틸리티 클래스
 */
public class TypeConversionUtils {

    private TypeConversionUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 빈 문자열을 null로 변환
     */
    public static String emptyToNull(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Object를 Integer로 변환
     */
    public static Integer toInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Integer i) return i;
        if (v instanceof Long l) return l.intValue();
        if (v instanceof BigInteger bi) return bi.intValue();
        if (v instanceof Number n) return n.intValue();
        return Integer.parseInt(String.valueOf(v));
    }

    /**
     * Object를 Boolean으로 변환
     */
    public static Boolean toBool(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean b) return b;
        if (v instanceof Integer i) return i != 0;
        if (v instanceof Long l) return l != 0L;
        if (v instanceof BigInteger bi) return bi.intValue() != 0;
        String s = String.valueOf(v).trim();
        return "1".equals(s) || "true".equalsIgnoreCase(s) || "Y".equalsIgnoreCase(s);
    }
}
