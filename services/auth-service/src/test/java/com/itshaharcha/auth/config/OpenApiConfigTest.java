package com.itshaharcha.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void buildsOpenApiWithBearerScheme() {
        OpenAPI api = new OpenApiConfig().authServiceOpenApi();

        assertThat(api.getInfo().getTitle()).contains("Authentication Service");
        assertThat(api.getInfo().getVersion()).isEqualTo("v1");
        assertThat(api.getSecurity()).isNotEmpty();
        assertThat(api.getComponents().getSecuritySchemes()).containsKey("bearerAuth");
    }
}
