package com.booktalk_be.domain.member.controller;

import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.member.command.CreateMemberCommand;
import com.booktalk_be.domain.member.command.ModifyMemberCommand;
import com.booktalk_be.domain.member.command.ValidationMemberCommand;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.member.responseDto.MemberInformationResponse;
import com.booktalk_be.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

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
    public ResponseEntity<ResponseDto> modify(@RequestBody @Valid ModifyMemberCommand cmd, Authentication authentication) {
        Member member = memberService.modifyMember(cmd, authentication);
        System.out.println("modify new Member :" + member.getName());
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

    @PostMapping("/validation")
    @Tag(name = "Member validation API")
    @Operation(summary = "회원 중복 검증", description = "동일한 회원 아이디의 존재 여부를 조회합니다.")
    public ResponseEntity<ResponseDto> validationUserName(@RequestBody @Valid ValidationMemberCommand username)  {

        boolean isExistMember = memberService.validationEmail(username.getEmail());

        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(Collections.singletonMap("isExistMember", isExistMember))
                .build());
    }

    @GetMapping("/authentication")
    @Tag(name = "Member Information API")
    @Operation(summary = "회원 정보 조회", description = "현재 인증 된 회원 정보를 조회합니다.")
    public ResponseEntity<ResponseDto> getAuthenticationMember(Authentication authentication) {
        Member member = (Member) authentication.getPrincipal();
        System.out.println("게또 이메일"+ member.getEmail());
        MemberInformationResponse memberDto = memberService.getAuthenticationMember(member);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResponseDto.builder()
                        .code(401)
                        .data(Collections.singletonMap("error", "zz"))
                        .build());
    }

    @GetMapping("/list")
    @Tag(name = "Member List API")
    @Operation(summary = "회원 전체 목록 조회", description = "회원 전체 목록을 조회합니다..")
    public ResponseEntity<ResponseDto> getMemberList() {
        List<MemberInformationResponse> memberListDto = memberService.getMemberAllList();
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(memberListDto)
                .build());
    }

    @PostMapping("/role/{userid}")
    @Tag(name = "Member Role Manage API")
    @Operation(summary = "회원 권한 변경", description = "회원의 권한을 수정합니다.")
    public ResponseEntity<ResponseDto> modifyMemberRole(@PathVariable String userid, @RequestParam String role) {
        Member modifyRoleMember = memberService.modifyRole(userid, role);
        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .data(modifyRoleMember)
                .build());
    }
}
