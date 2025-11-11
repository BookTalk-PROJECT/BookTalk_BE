package com.booktalk_be.domain.member.responseDto;

import com.booktalk_be.domain.auth.model.entity.AuthenticateType;
import com.booktalk_be.domain.auth.model.entity.AuthorityType;
import com.booktalk_be.domain.member.model.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class MemberInformationResponse {
    private int id;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDate birth;
    private LocalDate joinDate;
    private String gender;
    private String authType;
    private String authority;
//    private AuthenticateType authType;
//    private AuthorityType authority;

    public MemberInformationResponse(Member member) {
        this.id = member.getMemberId();
        this.name = member.getName();
        this.email = member.getEmail();
        this.phoneNumber = member.getPhoneNumber();
        this.address = member.getAddress();
        this.birth = member.getBirth();
        this.joinDate =   member.getRegTime().toLocalDate();
        this.authType = member.getAuthType().getType();
        this.authority = member.getAuthority().getType();
//        this.authType = member.getAuthType();
//        this.authority = member.getAuthority();
        this.gender = member.getGender();
    }
}
