package com.itshaharcha.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("IT-Shaharcha — User Service API")
                .description("Profiles, portfolio, certificates, education history")
                .version("v1"));
    }
}
