package com.booktalk_be.domain.auth.command;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginDTO {

    private String username;
    private String password;
}
