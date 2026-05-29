package com.itshaharcha.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void buildsOpenApiInfo() {
        OpenAPI api = new OpenApiConfig().userServiceOpenApi();

        assertThat(api.getInfo().getTitle()).contains("User Service");
        assertThat(api.getInfo().getVersion()).isEqualTo("v1");
    }
}
