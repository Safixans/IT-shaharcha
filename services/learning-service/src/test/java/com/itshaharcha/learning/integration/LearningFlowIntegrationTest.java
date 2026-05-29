package com.itshaharcha.learning.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end against a real Flyway-migrated Postgres. Learning validates (does not mint)
 * tokens, so JWTs are minted here with the same secret/issuer identity-service signs with.
 * Exercises: public catalog read (no auth), permission-gated authoring (TEACHER vs STUDENT),
 * the learner enroll -> start -> complete progress flow, analytics, and the 401 edge.
 */
@SpringBootTest(properties = {
        "app.events.kafka-enabled=false",
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "app.security.jwt.secret=" + LearningFlowIntegrationTest.SECRET,
        "app.security.jwt.issuer=" + LearningFlowIntegrationTest.ISSUER
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class LearningFlowIntegrationTest {

    static final String SECRET = "integration-test-secret-key-that-is-long-enough-32+";
    static final String ISSUER = "it-shaharcha-auth";

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("learning_db")
            .withUsername("itsh")
            .withPassword("itsh_secret");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

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
        mockMvc.perform(get("/api/v1/learning/tracks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void authoring_studentForbidden_teacherCreates() throws Exception {
        String trackBody = """
                {"title":"Frontend","slug":"frontend-guard","description":"x"}""";

        // STUDENT holds only *_READ -> 403 on TRACK_WRITE
        mockMvc.perform(post("/api/v1/learning/admin/tracks")
                        .header("Authorization", bearerStudent(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON).content(trackBody))
                .andExpect(status().isForbidden());

        // TEACHER holds *_WRITE -> 201
        mockMvc.perform(post("/api/v1/learning/admin/tracks")
                        .header("Authorization", bearerTeacher())
                        .contentType(MediaType.APPLICATION_JSON).content(trackBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Frontend"));
    }

    @Test
    void enroll_start_complete_progressReaches100() throws Exception {
        String teacher = bearerTeacher();

        // ---- author a course with one module + one lesson ----
        UUID trackId = createAndExtractId(teacher, "/api/v1/learning/admin/tracks", """
                {"title":"Java Track","slug":"java-track-flow"}""");

        String courseBody = objectMapper.writeValueAsString(java.util.Map.of(
                "trackId", trackId, "title", "Java Basics",
                "slug", "java-basics-flow", "level", "beginner", "estimatedMinutes", 120));
        UUID courseId = createAndExtractId(teacher, "/api/v1/learning/admin/courses", courseBody);

        String moduleBody = objectMapper.writeValueAsString(java.util.Map.of(
                "courseId", courseId, "title", "Intro", "order", 0));
        UUID moduleId = createAndExtractId(teacher, "/api/v1/learning/admin/modules", moduleBody);

        String lessonBody = objectMapper.writeValueAsString(java.util.Map.of(
                "moduleId", moduleId, "title", "Hello", "order", 0,
                "kind", "reading", "estimatedMinutes", 10));
        UUID lessonId = createAndExtractId(teacher, "/api/v1/learning/admin/lessons", lessonBody);

        // ---- learner enrolls (any authenticated role) ----
        UUID learner = UUID.randomUUID();
        String student = bearerStudent(learner);

        mockMvc.perform(post("/api/v1/learning/courses/" + courseId + ":enroll")
                        .header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("active"))
                .andExpect(jsonPath("$.data.progressPercent").value(0.0));

        // idempotent re-enroll
        mockMvc.perform(post("/api/v1/learning/courses/" + courseId + ":enroll")
                        .header("Authorization", student))
                .andExpect(status().isOk());

        // ---- start the lesson ----
        mockMvc.perform(post("/api/v1/learning/lessons/" + lessonId + ":start")
                        .header("Authorization", student))
                .andExpect(status().isNoContent());

        // ---- complete the only lesson -> 100% + course completed ----
        String completeBody = objectMapper.writeValueAsString(java.util.Map.of(
                "courseId", courseId, "moduleId", moduleId,
                "durationSeconds", 300, "scorePercent", 90.0));
        mockMvc.perform(post("/api/v1/learning/lessons/" + lessonId + ":complete")
                        .header("Authorization", student)
                        .contentType(MediaType.APPLICATION_JSON).content(completeBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true))
                .andExpect(jsonPath("$.data.courseProgressPercent").value(100.0));

        // ---- enrollment now reflects completion ----
        mockMvc.perform(get("/api/v1/learning/enrollments")
                        .header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].status").value("completed"))
                .andExpect(jsonPath("$.data.items[0].progressPercent").value(100.0));

        // ---- analytics summary rolls up the completed lesson ----
        mockMvc.perform(get("/api/v1/learning/analytics/summary")
                        .header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.domain").value("learning"))
                .andExpect(jsonPath("$.data.points").value(1))
                .andExpect(jsonPath("$.data.counters.lessonsCompleted").value(1))
                .andExpect(jsonPath("$.data.counters.enrollments").value(1));
    }

    @Test
    void enroll_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/learning/courses/" + UUID.randomUUID() + ":enroll"))
                .andExpect(status().isUnauthorized());
    }

    private UUID createAndExtractId(String bearer, String path, String body) throws Exception {
        MvcResult result = mockMvc.perform(post(path)
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        return UUID.fromString(data.get("id").asText());
    }
}
