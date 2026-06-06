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
import tools.jackson.databind.JsonNode;
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
 * Quiz lifecycle against a real Flyway-migrated Postgres: author creates + activates a quiz,
 * a student browses (active-only), starts (timezone-proof timing + answer-stripped problems),
 * submits and is auto-graded by value. Exercises the shared objective engine end to end.
 */
@SpringBootTest(properties = {
        "app.events.kafka-enabled=false",
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration",
        "app.security.jwt.secret=" + ObjectiveFlowIntegrationTest.SECRET,
        "app.security.jwt.issuer=" + ObjectiveFlowIntegrationTest.ISSUER
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class ObjectiveFlowIntegrationTest {

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
    void create_activate_start_submit_quiz() throws Exception {
        String teacher = token(UUID.randomUUID(), "ROLE_TEACHER");
        UUID studentId = UUID.randomUUID();
        String student = token(studentId, "ROLE_STUDENT");

        String createBody = """
                {
                  "title": "Capitals quiz",
                  "tags": ["geography"],
                  "durationSeconds": 600,
                  "questions": [
                    {"type": "RADIO", "prompt": "Capital of France?",
                     "options": [{"text": "Paris", "correct": true}, {"text": "Rome", "correct": false}]},
                    {"type": "INPUT", "prompt": "2 + 2 = ?", "correctAnswers": ["4", "four"]}
                  ]
                }
                """;

        String created = mockMvc.perform(post("/api/v1/assessment/quizzes")
                        .header("Authorization", teacher)
                        .contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.problemCount").value(2))
                .andExpect(jsonPath("$.data.active").value(false))
                // served problems never leak correctness
                .andExpect(jsonPath("$.data.problems[0].options[0].text").value("Paris"))
                .andExpect(jsonPath("$.data.problems[0].options[0].correct").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        UUID unitId = UUID.fromString(objectMapper.readTree(created).get("data").get("id").asString());

        // student sees nothing while inactive
        mockMvc.perform(get("/api/v1/assessment/quizzes").header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(0));

        // activate (quiz gate: >= 1 question)
        mockMvc.perform(post("/api/v1/assessment/quizzes/" + unitId + ":activate")
                        .header("Authorization", teacher))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true));

        // now visible to the student
        mockMvc.perform(get("/api/v1/assessment/quizzes").header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1));

        // start: timezone-proof timing + answer-stripped problems
        String session = mockMvc.perform(post("/api/v1/assessment/quizzes/" + unitId + ":start")
                        .header("Authorization", student))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.family").value("QUIZ"))
                .andExpect(jsonPath("$.data.timing.remainingSeconds").isNumber())
                .andExpect(jsonPath("$.data.timing.serverNow").exists())
                .andExpect(jsonPath("$.data.problems.length()").value(2))
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(session);
        UUID attemptId = UUID.fromString(root.get("data").get("attemptId").asString());
        JsonNode problems = root.get("data").get("problems");
        String radioId = null;
        String inputId = null;
        for (JsonNode p : problems) {
            if ("RADIO".equals(p.get("type").asString())) {
                radioId = p.get("problemId").asString();
            } else {
                inputId = p.get("problemId").asString();
            }
        }

        String submitBody = """
                {"answers": [
                    {"problemId": "%s", "values": ["Paris"]},
                    {"problemId": "%s", "values": ["four"]}
                ]}
                """.formatted(radioId, inputId);

        mockMvc.perform(post("/api/v1/assessment/attempts/" + attemptId + ":submit")
                        .header("Authorization", student)
                        .contentType(MediaType.APPLICATION_JSON).content(submitBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.correct").value(2))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.scorePercent").value(100.0));

        // history + report readable by the owner
        mockMvc.perform(get("/api/v1/assessment/attempts").header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1));
        mockMvc.perform(get("/api/v1/assessment/attempts/" + attemptId).header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    void start_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/assessment/quizzes/" + UUID.randomUUID() + ":start"))
                .andExpect(status().isUnauthorized());
    }
}
