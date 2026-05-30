package com.itshaharcha.assessment.integration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end against a real Flyway-migrated Postgres. Assessment validates (does not mint)
 * tokens, so JWTs are minted here with the same secret/issuer identity-service signs with.
 * Exercises: public catalog read, permission-gated authoring, and the candidate
 * start -> fetch questions -> submit -> scored result flow (synchronous scoring).
 */
@SpringBootTest(properties = {
        "app.events.kafka-enabled=false",
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "app.security.jwt.secret=" + AssessmentFlowIntegrationTest.SECRET,
        "app.security.jwt.issuer=" + AssessmentFlowIntegrationTest.ISSUER
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AssessmentFlowIntegrationTest {

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

    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private String mint(UUID sub, List<String> roles) {
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(sub.toString())
                .claim("type", "ACCESS")
                .claim("username", "user-" + sub)
                .claim("roles", roles)
                .expiration(Date.from(Instant.now().plusSeconds(600)))
                .signWith(KEY)
                .compact();
    }

    private String bearerTeacher() {
        return "Bearer " + mint(UUID.randomUUID(), List.of("ROLE_TEACHER"));
    }

    private String bearerStudent(UUID sub) {
        return "Bearer " + mint(sub, List.of("ROLE_STUDENT"));
    }

    @Test
    void publicCatalog_readableWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/assessment/exams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void authoring_studentForbidden_teacherCreates() throws Exception {
        String examBody = objectMapper.writeValueAsString(Map.of(
                "title", "SAT Guard Check", "examType", "SAT"));

        mockMvc.perform(post("/api/v1/assessment/admin/exams")
                        .header("Authorization", bearerStudent(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON).content(examBody))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/assessment/admin/exams")
                        .header("Authorization", bearerTeacher())
                        .contentType(MediaType.APPLICATION_JSON).content(examBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("SAT Guard Check"))
                .andExpect(jsonPath("$.data.examType").value("SAT"));
    }

    @Test
    void start_fetchQuestions_submit_isScoredSynchronously() throws Exception {
        String teacher = bearerTeacher();

        // ---- author an exam with one section + two single-choice questions ----
        String examBody = objectMapper.writeValueAsString(Map.of(
                "title", "MOCK Flow Exam", "examType", "MOCK",
                "durationMinutes", 60, "isRealExam", true));
        UUID examId = createAndExtractId(teacher, "/api/v1/assessment/admin/exams", examBody);

        String sectionBody = objectMapper.writeValueAsString(Map.of("name", "Reading", "order", 0));
        UUID sectionId = createAndExtractId(
                teacher, "/api/v1/assessment/admin/exams/" + examId + "/sections", sectionBody);

        String q1Body = objectMapper.writeValueAsString(Map.of(
                "prompt", "2 + 2 = ?", "kind", "single_choice", "order", 0, "points", 1,
                "choices", List.of(Map.of("id", "A", "text", "4"), Map.of("id", "B", "text", "5")),
                "correctAnswer", "A"));
        UUID q1 = createAndExtractId(
                teacher, "/api/v1/assessment/admin/sections/" + sectionId + "/questions", q1Body);

        String q2Body = objectMapper.writeValueAsString(Map.of(
                "prompt", "Capital of France?", "kind", "single_choice", "order", 1, "points", 1,
                "choices", List.of(Map.of("id", "A", "text", "Paris"), Map.of("id", "B", "text", "Rome")),
                "correctAnswer", "A"));
        UUID q2 = createAndExtractId(
                teacher, "/api/v1/assessment/admin/sections/" + sectionId + "/questions", q2Body);

        // ---- candidate starts a session ----
        UUID candidate = UUID.randomUUID();
        String student = bearerStudent(candidate);

        MvcResult started = mockMvc.perform(post("/api/v1/assessment/exams/" + examId + ":start")
                        .header("Authorization", student))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("in_progress"))
                .andReturn();
        UUID sessionId = UUID.fromString(objectMapper
                .readTree(started.getResponse().getContentAsString())
                .get("data").get("id").asString());

        // ---- re-fetch the questions (no correct answers leaked) ----
        mockMvc.perform(get("/api/v1/assessment/sessions/" + sessionId + "/questions")
                        .header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].prompt").value("2 + 2 = ?"))
                .andExpect(jsonPath("$.data[0].correctAnswer").doesNotExist());

        // ---- submit one correct, one wrong -> 1 / 2 ----
        String submitBody = objectMapper.writeValueAsString(Map.of("answers", List.of(
                Map.of("questionId", q1, "value", "A", "sectionId", sectionId),
                Map.of("questionId", q2, "value", "B", "sectionId", sectionId))));
        mockMvc.perform(post("/api/v1/assessment/sessions/" + sessionId + ":submit")
                        .header("Authorization", student)
                        .contentType(MediaType.APPLICATION_JSON).content(submitBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scaledScore").value(1.0))
                .andExpect(jsonPath("$.data.maxScore").value(2.0))
                .andExpect(jsonPath("$.data.examType").value("MOCK"));

        // ---- result endpoint returns the persisted score ----
        mockMvc.perform(get("/api/v1/assessment/sessions/" + sessionId + "/result")
                        .header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scaledScore").value(1.0));

        // ---- analytics summary rolls up the scored exam ----
        mockMvc.perform(get("/api/v1/assessment/analytics/summary")
                        .header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.domain").value("assessment"))
                .andExpect(jsonPath("$.data.counters.examsScored").value(1));
    }

    @Test
    void start_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/assessment/exams/" + UUID.randomUUID() + ":start"))
                .andExpect(status().isUnauthorized());
    }

    private UUID createAndExtractId(String bearer, String path, String body) throws Exception {
        MvcResult result = mockMvc.perform(post(path)
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        return UUID.fromString(data.get("id").asString());
    }
}
