package com.itshaharcha.analytics.integration;

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
 * End-to-end against a real Flyway-migrated Postgres. Drives the REST ingest fallback (the
 * same DomainEvent envelope the Kafka consumer folds) for one account across three domains,
 * then asserts the cross-domain reads: progress, milestones, the public leaderboard, the
 * caller's rank, the dashboard, and the uniform analytics sub-API (summary/activity).
 */
@SpringBootTest(properties = {
        "app.events.kafka-enabled=false",
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration",
        "app.security.jwt.secret=" + AnalyticsFlowIntegrationTest.SECRET,
        "app.security.jwt.issuer=" + AnalyticsFlowIntegrationTest.ISSUER
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AnalyticsFlowIntegrationTest {

    static final String SECRET = "integration-test-secret-key-that-is-long-enough-32+";
    static final String ISSUER = "it-shaharcha-auth";

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("analytics_db")
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

    private Map<String, Object> event(UUID eventId, String type, String source, UUID account,
                                      Map<String, Object> data) {
        return Map.of(
                "eventId", eventId.toString(),
                "type", type,
                "source", source,
                "specVersion", "1.0",
                "occurredAt", Instant.now().toString(),
                "actor", Map.of("accountId", account.toString(), "roles", List.of("ROLE_STUDENT")),
                "subject", Map.of("type", "thing", "id", "x"),
                "data", data);
    }

    @Test
    void ingest_thenReadsAcrossDomains() throws Exception {
        UUID learner = UUID.randomUUID();
        String admin = "Bearer " + mint(UUID.randomUUID(), List.of("ROLE_ADMIN"));
        String student = "Bearer " + mint(learner, List.of("ROLE_STUDENT"));

        UUID lessonEvt = UUID.randomUUID();
        String batch = objectMapper.writeValueAsString(List.of(
                event(lessonEvt, "learning.lesson.completed", "learning", learner,
                        Map.of("courseId", UUID.randomUUID().toString(), "durationSeconds", 540)),
                event(UUID.randomUUID(), "assessment.exam.scored", "assessment", learner,
                        Map.of("examId", UUID.randomUUID().toString(), "examType", "MOCK", "scaledScore", 7.5)),
                event(UUID.randomUUID(), "portfolio.published", "portfolio", learner,
                        Map.of("handle", "aziz")),
                event(UUID.randomUUID(), "bogus.event.happened", "learning", learner,
                        Map.of("x", 1))));

        // ---- STUDENT cannot ingest (admin only) -> 403 ----
        mockMvc.perform(post("/api/v1/analytics/events:ingest")
                        .header("Authorization", student)
                        .contentType(MediaType.APPLICATION_JSON).content(batch))
                .andExpect(status().isForbidden());

        // ---- ADMIN ingests: 3 accepted, 1 rejected (unknown type) ----
        mockMvc.perform(post("/api/v1/analytics/events:ingest")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(batch))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.received").value(4))
                .andExpect(jsonPath("$.data.accepted").value(3))
                .andExpect(jsonPath("$.data.duplicates").value(0))
                .andExpect(jsonPath("$.data.rejected.length()").value(1));

        // ---- re-deliver one event -> idempotent (duplicate) ----
        String replay = objectMapper.writeValueAsString(List.of(
                event(lessonEvt, "learning.lesson.completed", "learning", learner,
                        Map.of("courseId", UUID.randomUUID().toString(), "durationSeconds", 540))));
        mockMvc.perform(post("/api/v1/analytics/events:ingest")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(replay))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.accepted").value(0))
                .andExpect(jsonPath("$.data.duplicates").value(1));

        // ---- cross-domain progress: 50 + 100 + 30 = 180 across 3 domains ----
        mockMvc.perform(get("/api/v1/analytics/progress").header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountId").value(learner.toString()))
                .andExpect(jsonPath("$.data.totalPoints").value(180))
                .andExpect(jsonPath("$.data.domains.length()").value(3));

        // ---- milestones: first_lesson, first_exam, portfolio_published ----
        mockMvc.perform(get("/api/v1/analytics/milestones").header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(3));

        // ---- public leaderboard (no auth): the learner is the only ranked account ----
        mockMvc.perform(get("/api/v1/analytics/rankings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.period").value("all_time"))
                .andExpect(jsonPath("$.data.entries[0].accountId").value(learner.toString()))
                .andExpect(jsonPath("$.data.entries[0].rank").value(1))
                .andExpect(jsonPath("$.data.entries[0].points").value(180));

        // ---- domain-scoped leaderboard ----
        mockMvc.perform(get("/api/v1/analytics/rankings").param("domain", "assessment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.domain").value("assessment"))
                .andExpect(jsonPath("$.data.entries[0].points").value(100));

        // ---- caller's own rank: overall + per-domain entries ----
        mockMvc.perform(get("/api/v1/analytics/rankings/me").header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].rank").value(1))
                .andExpect(jsonPath("$.data[0].points").value(180));

        // ---- dashboard rolls up the three domain summaries ----
        mockMvc.perform(get("/api/v1/analytics/dashboard").header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summaries.length()").value(3))
                .andExpect(jsonPath("$.data.rank.points").value(180));

        // ---- uniform sub-API: cross-domain summary + activity replay ----
        mockMvc.perform(get("/api/v1/analytics/analytics/summary").header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.domain").value("analytics"))
                .andExpect(jsonPath("$.data.points").value(180));

        mockMvc.perform(get("/api/v1/analytics/analytics/activity").header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(3));
    }

    @Test
    void progress_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/progress"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void leaderboard_isPublic() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/rankings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
