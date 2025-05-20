package com.booktalk_be.domain.category.controller;

import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.category.command.CreateCategoryCommand;
import com.booktalk_be.domain.category.command.UpdateCategoryCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/community/category")
@Tag(name = "Community Category API", description = "커뮤니티 카테고리 API 입니다.")
public class CategoryController {

    @GetMapping("/list")
    @Tag(name = "Community Category API")
    @Operation(summary = "커뮤니티 카테고리 목록 조회", description = "카테고리 목록 정보를 조회합니다.")
    public ResponseEntity<ResponseDto> getList() {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PostMapping("/create")
    @Tag(name = "Community Category API")
    @Operation(summary = "커뮤니티 카테고리 등록", description = "새로운 카테고리를 등록합니다.")
    public ResponseEntity<ResponseDto> create(@RequestBody CreateCategoryCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PutMapping("/modify")
    @Tag(name = "Community Category API")
    @Operation(summary = "커뮤니티 카테고리 수정", description = "카테고리 상세 정보를 수정합니다.")
    public ResponseEntity<ResponseDto> modify(@RequestBody UpdateCategoryCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @DeleteMapping("/delete/{categoryId}")
    @Tag(name = "Community Category API")
    @Operation(summary = "커뮤니티 카테고리 삭제", description = "카테고리를 삭제합니다.")
    public ResponseEntity<ResponseDto> delete(@PathVariable String categoryId) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

}
