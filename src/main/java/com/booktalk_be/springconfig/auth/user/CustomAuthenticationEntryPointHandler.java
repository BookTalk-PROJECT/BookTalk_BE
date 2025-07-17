package com.booktalk_be.springconfig.auth.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component
public class CustomAuthenticationEntryPointHandler implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException authException) throws IOException, ServletException {
        log.info("[CustomAuthenticationEntryPointHandler] :: {}", authException.getMessage());
        log.info("[CustomAuthenticationEntryPointHandler] :: {}", request.getRequestURL());
        log.info("[CustomAuthenticationEntryPointHandler] :: 토근 정보가 만료되었거나 존재하지 않음");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        JSONObject returnJson = new JSONObject();
        returnJson.put("errorCode", HttpServletResponse.SC_UNAUTHORIZED);
        returnJson.put("errorMsg", HttpServletResponse.SC_UNAUTHORIZED);

        PrintWriter out = response.getWriter();
        out.print(returnJson);
    }
}
