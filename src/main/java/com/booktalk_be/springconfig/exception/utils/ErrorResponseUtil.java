package com.booktalk_be.springconfig.exception.utils;

import com.booktalk_be.springconfig.exception.Dto.ErrorDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ErrorResponseUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    private ErrorResponseUtil() {}

    /** 1) 예외 -> ErrorDto 매핑 (refresh/access 구분 가능) */
    public static ErrorDto fromJwtException(Exception e, boolean isRefresh) {

        if (e instanceof ExpiredJwtException) {
            return new ErrorDto(
                    HttpStatus.UNAUTHORIZED,
                    isRefresh ? "REFRESH_TOKEN_EXPIRED" : "ACCESS_TOKEN_EXPIRED",
                    isRefresh ? "Refresh Token has expired." : "Access Token has expired."
            );
        }

        if (e instanceof MalformedJwtException || e instanceof SignatureException || e instanceof IllegalArgumentException) {
            return new ErrorDto(
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_TOKEN",
                    "Invalid JWT Token."
            );
        }

        if (e instanceof JwtException je) {
            return new ErrorDto(
                    HttpStatus.UNAUTHORIZED,
                    "JWT_ERROR",
                    "JWT processing error: " + je.getMessage()
            );
        }

        return new ErrorDto(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "AUTHENTICATION_ERROR",
                "Authentication Error: " + e.getMessage()
        );
    }

    /** 2) ErrorDto -> 응답 body(Map) (필터/컨트롤러 공통 포맷) */
    public static Map<String, Object> buildBody(ErrorDto dto) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", dto.getStatus().value());
        body.put("error", dto.getStatus().getReasonPhrase());
        body.put("errorCode", dto.getErrorCode());
        body.put("message", dto.getMessage());
        body.put("timestamp", LocalDateTime.now().toString());
        return body;
    }

    /** 3) 컨트롤러용: ResponseEntity로 반환 */
    public static ResponseEntity<Map<String, Object>> toResponseEntity(ErrorDto dto) {
        return ResponseEntity.status(dto.getStatus()).body(buildBody(dto));
    }

    /** 4) 필터용: HttpServletResponse에 직접 write */
    public static void write(HttpServletResponse response, ErrorDto dto) throws IOException {
        response.setStatus(dto.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(mapper.writeValueAsString(buildBody(dto)));
    }
}
