package com.booktalk_be.domain.category.controller;

import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.category.command.CreateCategoryCommand;
import com.booktalk_be.domain.category.command.ReorderCategoryCommand;
import com.booktalk_be.domain.category.command.UpdateCategoryCommand;
import com.booktalk_be.domain.category.responseDto.CategoryResponse;
import com.booktalk_be.domain.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/community/category")
@Tag(name = "Community Category API", description = "커뮤니티 카테고리 API 입니다.")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    @Tag(name = "Community Category API")
    @Operation(summary = "커뮤니티 카테고리 목록 조회", description = "카테고리 목록 정보를 조회합니다.")
    public ResponseEntity<ResponseDto> getList() {
        List<CategoryResponse> res = categoryService.getList();
        return ResponseEntity.ok(ResponseDto.builder()
                .data(res)
                .code(200)
                .build());
    }

    @GetMapping("/admin/list/all")
    @Tag(name = "Community Category API")
    @Operation(summary = "커뮤니티 카테고리 전체 목록 조회", description = "카테고리 전체 목록 정보를 조회합니다.")
    public ResponseEntity<ResponseDto> getAllList() {
        List<CategoryResponse> res = categoryService.getAllList();
        return ResponseEntity.ok(ResponseDto.builder()
                .data(res)
                .code(200)
                .build());
    }

    @PostMapping("/create")
    @Tag(name = "Community Category API")
    @Operation(summary = "커뮤니티 카테고리 등록", description = "새로운 카테고리를 등록합니다.")
    public ResponseEntity<ResponseDto> create(@RequestBody @Valid CreateCategoryCommand cmd) {
        Integer savedCategoryId = categoryService.createCategory(cmd);
        return ResponseEntity.ok(ResponseDto.builder()
                .data(savedCategoryId)
                .code(200)
                .build());
    }

    @PatchMapping("/modify")
    @Tag(name = "Community Category API")
    @Operation(summary = "커뮤니티 카테고리 수정", description = "카테고리 상세 정보를 수정합니다.")
    public ResponseEntity<ResponseDto> modify(@RequestBody @Valid UpdateCategoryCommand cmd) {
        categoryService.editCategory(cmd);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @DeleteMapping("/delete/{categoryId}")
    @Tag(name = "Community Category API")
    @Operation(summary = "커뮤니티 카테고리 삭제", description = "카테고리를 삭제합니다.")
    public ResponseEntity<ResponseDto> delete(@PathVariable Integer categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PatchMapping("/reorder")
    @Tag(name = "Community Category API")
    @Operation(summary = "커뮤니티 카테고리 순서 변경", description = "카테고리 표시 순서를 일괄 변경합니다.")
    public ResponseEntity<ResponseDto> reorder(@RequestBody @Valid ReorderCategoryCommand cmd) {
        categoryService.reorderCategories(cmd);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

}
