package com.booktalk_be.domain.likes.controller;

import com.booktalk_be.common.utils.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/likes")
@Tag(name = "Likes API", description = "좋아요 API 입니다.")
public class LikesController {

    @PostMapping("/set/{code}")
    @Tag(name = "Likes API")
    @Operation(summary = "좋아요 등록", description = "특정 게시물에 좋아요를 등록합니다.")
    public ResponseEntity<ResponseDto> setLikes(@PathVariable String code) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PostMapping("/reset/{code}")
    @Tag(name = "Likes API")
    @Operation(summary = "좋아요 해제", description = "특정 게시물에 좋아요를 해제합니다.")
    public ResponseEntity<ResponseDto> resetLikes(@PathVariable String code) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

}
