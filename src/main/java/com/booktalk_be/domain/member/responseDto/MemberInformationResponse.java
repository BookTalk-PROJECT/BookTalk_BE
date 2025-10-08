package com.booktalk_be.domain.member.responseDto;

import com.booktalk_be.domain.auth.model.entity.AuthenticateType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class MemberInformationResponse {
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDate birth;
    private String gender;
    private AuthenticateType authType;
}
