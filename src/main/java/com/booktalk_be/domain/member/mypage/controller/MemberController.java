package com.booktalk_be.domain.member.mypage.controller;

import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.member.mypage.command.CreateMemberCommand;
import com.booktalk_be.domain.member.mypage.command.ModifyMemberCommand;
import com.booktalk_be.domain.member.mypage.model.entity.Member;
import com.booktalk_be.domain.member.mypage.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
@Tag(name = "Member API", description = "멤버 관련 API 입니다.")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/create")
    @Tag(name = "Member Create API")
    @Operation(summary = "신규 회원 등록", description = "새로운 멤버를 등록합니다.")
    public ResponseEntity<ResponseDto> create(@RequestBody @Valid CreateMemberCommand cmd) {
        Member member = memberService.createMember(cmd);
        System.out.println("created new Member :" + member.getName());
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @PatchMapping("/modify")
    @Tag(name = "Member Modify API")
    @Operation(summary = "회원 정보 수정", description = "기존 멤버의 정보를 수정합니다.")
    public ResponseEntity<ResponseDto> modify(@RequestBody @Valid ModifyMemberCommand cmd) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @DeleteMapping("/delete/{userid}")
    @Tag(name = "Member Delete API")
    @Operation(summary = "회원 삭제", description = "기존 멤버의 정보를 삭제합니다.")
    public ResponseEntity<ResponseDto> delete(@PathVariable int userid) {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }

    @GetMapping("/activity")
    @Tag(name = "MyPage API")
    @Operation(summary = "마이 페이지 내 최근 활동", description = "마이 페이지 메인의 내 최근 활동 목록을 조회합니다.")
    public ResponseEntity<ResponseDto> getRecentActivityList(@RequestParam(value = "pageNum", required = true) Integer pageNum,
                                                             @RequestBody @Valid PostSearchCondCommand cmd)  {
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }
}
