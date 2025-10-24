package com.booktalk_be.domain.gathering.command;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RecruitRequestCommand {
    @NotEmpty
    private List<AnswerItem> answers;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AnswerItem {
        private Long questionId;
        private String answer;
    }
}