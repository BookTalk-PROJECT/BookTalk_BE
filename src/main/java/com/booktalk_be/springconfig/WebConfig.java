package com.booktalk_be.springconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // "file:" 접두어를 명시적으로 붙임
        String uploadPath = Paths.get("uploads/images").toAbsolutePath().toString();

        System.out.println("이미지 리소스 경로: file:" + uploadPath + "/");

        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
