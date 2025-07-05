package com.booktalk_be.domain.gathering.command;


import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreateRecruitRequest {
    private List<String> recruits;
}
