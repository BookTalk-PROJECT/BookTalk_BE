package com.booktalk_be.domain.member.join.command;

import com.booktalk_be.domain.board.model.entity.Board;
import com.booktalk_be.domain.member.auth.model.entity.AuthenticateType;
import com.booktalk_be.domain.member.auth.model.entity.AuthorityType;
import com.booktalk_be.domain.member.auth.model.entity.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CreateMemberCommand {

    @NotNull
    private String email;

    @NotNull
    private String name;

    @NotNull
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

    public Member toEntity() {
        return Member.builder()
                .email(this.email)
                .name(this.name)
                .authType(this.authType)
                .password(this.password)
                .phoneNumber(this.phoneNumber)
                .address(this.address)
                .gender(this.gender)
                .birth(this.birth)
                .build();
    }
}
