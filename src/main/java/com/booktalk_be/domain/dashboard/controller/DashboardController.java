package com.booktalk_be.domain.dashboard.controller;

import com.booktalk_be.common.utils.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard API", description = "대시보드 API 입니다.")
public class DashboardController {

    @GetMapping("/hello")
    @Tag(name = "Dashboard API")
    @Operation(summary = "Hello 조회", description = "Hello 정보를 조회합니다.")
    public ResponseEntity<?> findHello() {
        return ResponseEntity.ok(ResponseDto.builder().msg("HelloWorld!").code(200).build());
    }

    @PostMapping("/hello")
    @Tag(name = "Dashboard API")
    @Operation(summary = "Hello 조회", description = "Hello 정보를 조회합니다.")
    public ResponseEntity<?> createHello(@RequestBody HelloTest body) {
        return ResponseEntity.ok(ResponseDto.builder().msg(body.getHello()).code(200).build());
    }
}
