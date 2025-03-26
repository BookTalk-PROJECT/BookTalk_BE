package com.booktalk_be.common.utils;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseDto {
    private String msg;
    private int code;
    private Object data;
}
