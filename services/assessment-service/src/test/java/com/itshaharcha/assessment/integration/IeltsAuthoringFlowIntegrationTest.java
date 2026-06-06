package com.itshaharcha.assessment.integration;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.json.JsonMapper;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * IELTS authoring: the parser strips answers from the served {@code sectionData}, and the
 * answer-bearing {@code originalSectionData} is returned ONLY to authors (never to students).
 */
@SpringBootTest(properties = {
        "app.events.kafka-enabled=false",
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration",
        "app.security.jwt.secret=" + IeltsAuthoringFlowIntegrationTest.SECRET,
        "app.security.jwt.issuer=" + IeltsAuthoringFlowIntegrationTest.ISSUER
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class IeltsAuthoringFlowIntegrationTest {

    static final String SECRET = "integration-test-secret-key-that-is-long-enough-32+";
    static final String ISSUER = "it-shaharcha-auth";

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("assessment_db")
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

    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private String token(UUID sub, String role) {
        return "Bearer " + Jwts.builder()
                .issuer(ISSUER).subject(sub.toString())
                .claim("type", "ACCESS").claim("username", "u-" + sub)
                .claim("roles", List.of(role))
                .expiration(Date.from(Instant.now().plusSeconds(600)))
                .signWith(KEY).compact();
    }

    @Test
    void originalSectionData_isAuthorOnly() throws Exception {
        String teacher = token(UUID.randomUUID(), "ROLE_TEACHER");
        String student = token(UUID.randomUUID(), "ROLE_STUDENT");

        String created = mockMvc.perform(post("/api/v1/assessment/ielts/listening")
                        .header("Authorization", teacher)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Section 1", "audioId": "%s",
                                 "questions": "<p>The animal is <input type=\\"text\\" value=\\"snails\\" /></p>"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.problemCount").value(1))
                // served HTML is answer-stripped but carries the problem id
                .andExpect(jsonPath("$.data.sectionData", org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("value=\"snails\""))))
                .andExpect(jsonPath("$.data.sectionData", org.hamcrest.Matchers.containsString("data-problem-id")))
                // author sees the answer-bearing original
                .andExpect(jsonPath("$.data.originalSectionData", org.hamcrest.Matchers.containsString("snails")))
                .andReturn().getResponse().getContentAsString();

        String unitId = objectMapper.readTree(created).get("data").get("id").asString();

        // Author GET still includes the original
        mockMvc.perform(get("/api/v1/assessment/ielts/listening/" + unitId).header("Authorization", teacher))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.originalSectionData").exists());

        // Student GET must NOT leak the answer-bearing original
        mockMvc.perform(get("/api/v1/assessment/ielts/listening/" + unitId).header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sectionData").exists())
                .andExpect(jsonPath("$.data.originalSectionData").doesNotExist());
    }
}
