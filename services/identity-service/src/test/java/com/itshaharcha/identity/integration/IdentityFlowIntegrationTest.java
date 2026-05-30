package com.itshaharcha.identity.integration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end of the identity happy path against a real Postgres (Flyway-migrated, with the
 * seeded ROLE_STUDENT). Exercises register -> login -> /identity/me -> refresh (rotation) ->
 * logout, plus the duplicate/validation/unauthorized/admin-guard edges.
 */
@SpringBootTest(properties = {
        "app.auth.auto-activate=true",
        "app.events.kafka-enabled=false",
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration,"
                + "org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration",
        "app.security.jwt.secret=integration-test-secret-key-that-is-long-enough-32+"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class IdentityFlowIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("identity_db")
            .withUsername("itsh")
            .withPassword("itsh_secret");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private JsonMapper objectMapper;

    @Test
    void register_login_me_refresh_logout_happyPath() throws Exception {
        String registerBody = """
                {"email":"flow@example.com","username":"flowuser","password":"Password1"}""";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("flowuser"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // ---- login ----
        String loginBody = """
                {"identifier":"flowuser","password":"Password1"}""";
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode loginData = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data");
        String accessToken = loginData.get("accessToken").asString();
        String refreshToken = loginData.get("refreshToken").asString();

        // ---- /identity/me with the access token ----
        mockMvc.perform(get("/api/v1/identity/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("flowuser"));

        // ---- refresh (rotation) ----
        String refreshBody = objectMapper.writeValueAsString(
                java.util.Map.of("refreshToken", refreshToken));
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON).content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        String newRefresh = objectMapper.readTree(refreshResult.getResponse().getContentAsString())
                .get("data").get("refreshToken").asString();
        assertThat(newRefresh).isNotEqualTo(refreshToken); // rotated

        // ---- the old refresh token is now revoked ----
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON).content(refreshBody))
                .andExpect(status().isUnauthorized());

        // ---- logout with the rotated token (204) ----
        String logoutBody = objectMapper.writeValueAsString(
                java.util.Map.of("refreshToken", newRefresh));
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON).content(logoutBody))
                .andExpect(status().isNoContent());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        String body = """
                {"email":"dup@example.com","username":"dupuser1","password":"Password1"}""";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        String body2 = """
                {"email":"dup@example.com","username":"dupuser2","password":"Password1"}""";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body2))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void register_invalidPayload_returns400() throws Exception {
        String body = """
                {"email":"not-an-email","username":"x","password":"weak"}""";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void me_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/identity/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accounts_nonAdmin_returns403() throws Exception {
        String registerBody = """
                {"email":"plain@example.com","username":"plainuser","password":"Password1"}""";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(registerBody))
                .andExpect(status().isCreated());

        String loginBody = """
                {"identifier":"plainuser","password":"Password1"}""";
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data").get("accessToken").asString();

        mockMvc.perform(get("/api/v1/identity/accounts")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }
}
