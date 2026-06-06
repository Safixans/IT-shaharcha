package com.itshaharcha.assessment.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * IELTS Writing is stored and TEACHER-graded. This drives author → activate → student start →
 * submit (PENDING_GRADING) → teacher grades, with the teacher↔student authorization resolved by
 * forwarding the caller's token to identity-service, which is stubbed with WireMock.
 */
@SpringBootTest(properties = {
        "app.events.kafka-enabled=false",
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration",
        "app.security.jwt.secret=" + WritingGradingFlowIntegrationTest.SECRET,
        "app.security.jwt.issuer=" + WritingGradingFlowIntegrationTest.ISSUER
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class WritingGradingFlowIntegrationTest {

    static final String SECRET = "integration-test-secret-key-that-is-long-enough-32+";
    static final String ISSUER = "it-shaharcha-auth";

    // Stubs identity-service (teacher↔student authorization). Started at class load so its port
    // is available when @DynamicPropertySource wires app.identity.base-url.
    static final WireMockServer identity = new WireMockServer(WireMockConfiguration.options().dynamicPort());

    static {
        identity.start();
    }

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("assessment_db")
            .withUsername("itsh")
            .withPassword("itsh_secret");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.identity.base-url", () -> "http://localhost:" + identity.port());
    }

    @BeforeEach
    void resetStubs() {
        identity.resetAll();
    }

    @AfterAll
    static void stopWireMock() {
        identity.stop();
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
    void writing_submittedThenTeacherGrades() throws Exception {
        String teacher = token(UUID.randomUUID(), "ROLE_TEACHER");
        UUID studentId = UUID.randomUUID();
        String student = token(studentId, "ROLE_STUDENT");

        // author a Task-2 writing unit (no image required) and activate it
        String created = mockMvc.perform(post("/api/v1/assessment/ielts/writing")
                        .header("Authorization", teacher)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Opinion essay", "task": "TASK_2",
                                 "prompt": "Some people think... Discuss.", "durationSeconds": 2400}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID unitId = UUID.fromString(objectMapper.readTree(created).get("data").get("id").asString());

        mockMvc.perform(post("/api/v1/assessment/ielts/writing/" + unitId + ":activate")
                        .header("Authorization", teacher))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true));

        // student starts and submits an essay -> PENDING_GRADING
        String session = mockMvc.perform(post("/api/v1/assessment/ielts/writing/" + unitId + ":start")
                        .header("Authorization", student))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.family").value("IELTS_WRITING"))
                .andExpect(jsonPath("$.data.prompt").value("Some people think... Discuss."))
                .andReturn().getResponse().getContentAsString();
        UUID attemptId = UUID.fromString(objectMapper.readTree(session).get("data").get("attemptId").asString());

        mockMvc.perform(post("/api/v1/assessment/attempts/" + attemptId + ":submit")
                        .header("Authorization", student)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"essay\": \"My structured opinion essay body...\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING_GRADING"));

        // identity stub: the student belongs to this teacher's roster
        String member = """
                {"studentId": "%s", "username": "s", "email": "s@x.io", "groupId": "%s"}
                """.formatted(studentId, UUID.randomUUID());
        identity.stubFor(WireMock.get(urlPathEqualTo("/api/v1/identity/me/students"))
                .willReturn(okJson("{\"success\": true, \"data\": [" + member + "]}")));
        identity.stubFor(WireMock.get(urlPathEqualTo("/api/v1/identity/me/students/" + studentId))
                .willReturn(okJson("{\"success\": true, \"data\": " + member + "}")));

        // queue shows the pending submission
        mockMvc.perform(get("/api/v1/assessment/grading/queue").header("Authorization", teacher))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].attemptId").value(attemptId.toString()));

        // grade it
        mockMvc.perform(post("/api/v1/assessment/grading/" + attemptId)
                        .header("Authorization", teacher)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"band": 7.0,
                                 "criteria": {"taskAchievement": 7.0, "coherenceCohesion": 7.0,
                                              "lexicalResource": 6.5, "grammaticalRange": 7.0},
                                 "feedback": "Clear position, good cohesion."}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("GRADED"))
                .andExpect(jsonPath("$.data.band").value(7.0))
                .andExpect(jsonPath("$.data.feedback").value("Clear position, good cohesion."));

        // queue is now empty
        mockMvc.perform(get("/api/v1/assessment/grading/queue").header("Authorization", teacher))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(0));
    }

    @Test
    void grading_rejectsNonOwnTeacher() throws Exception {
        String teacher = token(UUID.randomUUID(), "ROLE_TEACHER");
        UUID studentId = UUID.randomUUID();
        String student = token(studentId, "ROLE_STUDENT");

        String created = mockMvc.perform(post("/api/v1/assessment/ielts/writing")
                        .header("Authorization", teacher)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Essay", "task": "TASK_2", "prompt": "Discuss.", "durationSeconds": 2400}
                                """))
                .andReturn().getResponse().getContentAsString();
        UUID unitId = UUID.fromString(objectMapper.readTree(created).get("data").get("id").asString());
        mockMvc.perform(post("/api/v1/assessment/ielts/writing/" + unitId + ":activate")
                .header("Authorization", teacher));

        String session = mockMvc.perform(post("/api/v1/assessment/ielts/writing/" + unitId + ":start")
                        .header("Authorization", student))
                .andReturn().getResponse().getContentAsString();
        UUID attemptId = UUID.fromString(objectMapper.readTree(session).get("data").get("attemptId").asString());
        mockMvc.perform(post("/api/v1/assessment/attempts/" + attemptId + ":submit")
                .header("Authorization", student)
                .contentType(MediaType.APPLICATION_JSON).content("{\"essay\": \"text\"}"));

        // identity says this student is NOT the teacher's -> 404 -> grading forbidden
        identity.stubFor(WireMock.get(urlPathEqualTo("/api/v1/identity/me/students/" + studentId))
                .willReturn(aResponse().withStatus(404)));

        mockMvc.perform(post("/api/v1/assessment/grading/" + attemptId)
                        .header("Authorization", teacher)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"band\": 6.0}"))
                .andExpect(status().isForbidden());
    }
}
