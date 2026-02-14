package com.booktalk_be.springconfig.exception;

import com.booktalk_be.common.utils.ResponseDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for common exceptions across the application.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles optimistic locking failures when concurrent updates occur.
     * Returns HTTP 409 Conflict to indicate the resource was modified by another user.
     */
    @ExceptionHandler({OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ResponseDto> handleOptimisticLockingFailure(Exception ex) {
        log.warn("Optimistic locking failure occurred: {}", ex.getMessage());

        ResponseDto response = ResponseDto.builder()
                .code(HttpStatus.CONFLICT.value())
                .msg("다른 사용자가 수정 중입니다. 새로고침 후 다시 시도해주세요.")
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handles entity not found exceptions.
     * Returns HTTP 404 Not Found.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ResponseDto> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());

        ResponseDto response = ResponseDto.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .msg("요청한 데이터를 찾을 수 없습니다.")
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles illegal argument exceptions.
     * Returns HTTP 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ResponseDto response = ResponseDto.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .msg(ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
