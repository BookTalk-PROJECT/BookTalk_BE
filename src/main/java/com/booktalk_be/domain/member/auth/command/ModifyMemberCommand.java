package com.booktalk_be.domain.member.auth.command;

import com.booktalk_be.domain.member.auth.model.entity.Member;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ModifyMemberCommand {

    @NotNull
    private String password;

    private String phoneNumber;

    private String address;

    private String gender;

    private LocalDate birth;

    @NotNull
    private Boolean delYn = false;

    public Member toEntity() {
        return Member.builder()
                .password(this.password)
                .phoneNumber(this.phoneNumber)
                .address(this.address)
                .gender(this.gender)
                .birth(this.birth)
                .build();
    }
}
