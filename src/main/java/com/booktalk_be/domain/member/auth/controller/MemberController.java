package com.booktalk_be.domain.member.auth.controller;

import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.member.auth.command.CreateMemberCommand;
import com.booktalk_be.domain.member.auth.command.ModifyMemberCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
@Tag(name = "Member Join API", description = "회원 가입 관련 API 입니다.")
public class MemberController {

    @PostMapping("/create")
    @Tag(name = "Member Create API")
    @Operation(summary = "신규 회원 등록", description = "새로운 멤버를 등록합니다.")
    public ResponseEntity<ResponseDto> join(@RequestBody @Valid CreateMemberCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PostMapping("/modify")
    @Tag(name = "Member Modify API")
    @Operation(summary = "회원 정보 수정", description = "기존 멤버의 정보를 수정합니다.")
    public ResponseEntity<ResponseDto> join(@RequestBody @Valid ModifyMemberCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

}
