package com.booktalk_be.springconfig.swagger;

import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    //http://localhost:8080/swagger-ui/index.html
    @Bean
    public GroupedOpenApi groupedOpenApi() {
        String[] paths = {"/**"};
        String[] packagesToScan = {"com.booktalk_be"};
        return GroupedOpenApi.builder()
                .group("v1-definition")
                .pathsToMatch(paths)
                .packagesToScan(packagesToScan)
                .addOpenApiCustomizer(productApiCustomizer())
                .build();
    }

    private OpenApiCustomizer productApiCustomizer() {
        return openApi -> openApi.info(new Info()
                        .title("API Doc")
                        .description("API 명세서입니다")
                        .version("v0.0.1"));
    }
}