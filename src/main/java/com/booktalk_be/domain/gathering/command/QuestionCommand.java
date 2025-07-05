package com.booktalk_be.domain.gathering.command;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuestionCommand {
    private int id;
    private String question;
}
