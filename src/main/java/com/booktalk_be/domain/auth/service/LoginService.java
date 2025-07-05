package com.booktalk_be.domain.auth.service;

import com.booktalk_be.domain.auth.command.LoginDTO;

import java.util.Map;

public interface LoginService {
    Map<String, String> login(LoginDTO loginData);
}
