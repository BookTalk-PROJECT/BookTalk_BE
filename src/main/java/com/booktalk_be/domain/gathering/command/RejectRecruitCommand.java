package com.booktalk_be.domain.gathering.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RejectRecruitCommand {
    @NotBlank(message = "모임 코드는 필수입니다.")
    private String gathering_code;

    @NotNull(message = "신청자 ID는 필수입니다.")
    private int applicant_id;

    @NotBlank(message = "거절 사유는 필수입니다.")
    private String reject_reason;
}
