package com.booktalk_be.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonPrinter {
    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static void print(Object obj) {
        try {
            String json = mapper.writeValueAsString(obj);
            System.out.println(json);
        } catch (Exception e) {
            System.out.println("JSON 출력 오류: " + e.getMessage());
        }
    }
}
