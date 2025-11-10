package com.booktalk_be.domain.gathering.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeleteGatheringCommand {
    @NotBlank(message = "삭제 사유를 입력해주세요.")
    private String reason;
}
