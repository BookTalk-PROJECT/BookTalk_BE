package com.booktalk_be.dashboard.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HelloTest {
    @NotNull(message = "hello는 비워둘 수 없습니다.")
    @Schema(description = "hello 메세지")
    public String hello;
}
