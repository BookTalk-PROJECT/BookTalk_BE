package com.booktalk_be.springconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.image-dir}")
    private String imageUploadDir;

    @Value("${app.upload.url-prefix:/uploads/images}")
    private String imageUrlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = Paths.get(imageUploadDir).toAbsolutePath().toString();

        System.out.println("이미지 리소스 경로: file:" + uploadPath + "/");

        registry.addResourceHandler(imageUrlPrefix + "/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
