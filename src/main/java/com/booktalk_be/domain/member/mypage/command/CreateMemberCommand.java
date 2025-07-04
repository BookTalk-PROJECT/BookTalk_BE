package com.booktalk_be.domain.member.mypage.command;

import com.booktalk_be.domain.auth.model.entity.AuthenticateType;
import com.booktalk_be.domain.auth.model.entity.AuthorityType;
import com.booktalk_be.domain.member.mypage.model.entity.Member;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CreateMemberCommand {

    @NotNull
    private String email;

    @NotNull
    private String name;


    private AuthenticateType authType;

    @NotNull
    private String password;

    private String phoneNumber;

    private String address;

    private String gender;

    private LocalDate birth;

    private AuthorityType authority;

    @NotNull
    private Boolean delYn = false;

    public Member toEntity(String password) {
        return Member.builder()
                .email(this.email)
                .name(this.name)
                .authType(this.authType)
                .password(password)
                .phoneNumber(this.phoneNumber)
                .address(this.address)
                .gender(this.gender)
                .birth(this.birth)
                .build();
    }
}
