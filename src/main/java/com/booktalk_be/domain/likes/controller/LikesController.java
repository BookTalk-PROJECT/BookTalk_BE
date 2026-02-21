package com.booktalk_be.domain.likes.controller;

import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.likes.service.LikesService;
import com.booktalk_be.domain.member.model.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
@Tag(name = "Likes API", description = "좋아요 API 입니다.")
public class LikesController {

    private final LikesService likesService;

    @PostMapping("/set/{code}")
    @Tag(name = "Likes API")
    @Operation(summary = "좋아요 등록", description = "특정 게시물에 좋아요를 등록합니다.")
    public ResponseEntity<ResponseDto> setLikes(
            @PathVariable String code,
            Authentication authentication) {
        Member member = (Member) authentication.getPrincipal();
        likesService.addLike(code, member);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data("좋아요가 등록되었습니다.")
                .build());
    }

    @PostMapping("/reset/{code}")
    @Tag(name = "Likes API")
    @Operation(summary = "좋아요 해제", description = "특정 게시물에 좋아요를 해제합니다.")
    public ResponseEntity<ResponseDto> resetLikes(
            @PathVariable String code,
            Authentication authentication) {
        Member member = (Member) authentication.getPrincipal();
        likesService.removeLike(code, member);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data("좋아요가 해제되었습니다.")
                .build());
    }

}
