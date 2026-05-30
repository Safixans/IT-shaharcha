package com.itshaharcha.portfolio.integration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end against a real Flyway-migrated Postgres. Portfolio validates (does not mint)
 * tokens, so JWTs are minted here with the same secret/issuer identity-service signs with.
 * Exercises: file upload -> certificate register -> reviewer verify (TEACHER vs STUDENT
 * 403) -> education + items -> assembled portfolio -> publish -> public view (no auth).
 */
@SpringBootTest(properties = {
        "app.events.kafka-enabled=false",
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration",
        "app.security.jwt.secret=" + PortfolioFlowIntegrationTest.SECRET,
        "app.security.jwt.issuer=" + PortfolioFlowIntegrationTest.ISSUER
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class PortfolioFlowIntegrationTest {

    static final String SECRET = "integration-test-secret-key-that-is-long-enough-32+";
    static final String ISSUER = "it-shaharcha-auth";

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("portfolio_db")
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
    void upload_certify_verify_publish_publicView() throws Exception {
        UUID owner = UUID.randomUUID();
        String student = bearerStudent(owner);

        // ---- upload a file blob ----
        MockMultipartFile pdf = new MockMultipartFile(
                "file", "diploma.pdf", "application/pdf", "%PDF-1.7 fake".getBytes(StandardCharsets.UTF_8));
        MvcResult uploaded = mockMvc.perform(multipart("/api/v1/portfolio/files")
                        .file(pdf)
                        .header("Authorization", student))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fileId").exists())
                .andExpect(jsonPath("$.data.contentType").value("application/pdf"))
                .andReturn();
        UUID fileId = UUID.fromString(objectMapper
                .readTree(uploaded.getResponse().getContentAsString())
                .get("data").get("fileId").asString());

        // ---- register a certificate referencing the file (status PENDING) ----
        String certBody = objectMapper.writeValueAsString(java.util.Map.of(
                "title", "AWS Certified Cloud Practitioner",
                "issuer", "Amazon Web Services",
                "fileId", fileId));
        MvcResult certCreated = mockMvc.perform(post("/api/v1/portfolio/certificates")
                        .header("Authorization", student)
                        .contentType(MediaType.APPLICATION_JSON).content(certBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();
        UUID certId = UUID.fromString(objectMapper
                .readTree(certCreated.getResponse().getContentAsString())
                .get("data").get("id").asString());

        // ---- STUDENT cannot verify (lacks CERTIFICATE_VERIFY) -> 403 ----
        mockMvc.perform(post("/api/v1/portfolio/certificates/" + certId + ":verify")
                        .header("Authorization", student)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"verified\":true}"))
                .andExpect(status().isForbidden());

        // ---- TEACHER (reviewer) verifies -> VERIFIED ----
        mockMvc.perform(post("/api/v1/portfolio/certificates/" + certId + ":verify")
                        .header("Authorization", bearerTeacher())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"verified\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("VERIFIED"))
                .andExpect(jsonPath("$.data.verifiedAt").exists());

        // ---- add an education entry ----
        String eduBody = objectMapper.writeValueAsString(java.util.Map.of(
                "institution", "Tashkent University of Information Technologies",
                "degree", "BSc", "fieldOfStudy", "Computer Science"));
        mockMvc.perform(post("/api/v1/portfolio/education")
                        .header("Authorization", student)
                        .contentType(MediaType.APPLICATION_JSON).content(eduBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.institution").value(
                        "Tashkent University of Information Technologies"));

        // ---- add a portfolio item ----
        String itemBody = objectMapper.writeValueAsString(java.util.Map.of(
                "kind", "project", "title", "Task Manager App",
                "url", "https://github.com/aziz/task-manager",
                "tags", List.of("react", "spring-boot")));
        mockMvc.perform(post("/api/v1/portfolio/items")
                        .header("Authorization", student)
                        .contentType(MediaType.APPLICATION_JSON).content(itemBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.kind").value("project"))
                .andExpect(jsonPath("$.data.tags.length()").value(2));

        // ---- the assembled portfolio reflects everything ----
        mockMvc.perform(get("/api/v1/portfolio/me")
                        .header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountId").value(owner.toString()))
                .andExpect(jsonPath("$.data.visibility").value("private"))
                .andExpect(jsonPath("$.data.certificates.length()").value(1))
                .andExpect(jsonPath("$.data.education.length()").value(1))
                .andExpect(jsonPath("$.data.items.length()").value(1));

        // ---- publish at a public handle ----
        String publishBody = objectMapper.writeValueAsString(java.util.Map.of(
                "handle", "aziz-karimov", "visibility", "public"));
        mockMvc.perform(post("/api/v1/portfolio/me:publish")
                        .header("Authorization", student)
                        .contentType(MediaType.APPLICATION_JSON).content(publishBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.handle").value("aziz-karimov"))
                .andExpect(jsonPath("$.data.visibility").value("public"))
                .andExpect(jsonPath("$.data.publishedAt").exists());

        // ---- public view by handle WITHOUT auth ----
        mockMvc.perform(get("/api/v1/portfolio/public/aziz-karimov"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.handle").value("aziz-karimov"))
                .andExpect(jsonPath("$.data.certificates.length()").value(1));

        // ---- analytics summary rolls up the domain ----
        mockMvc.perform(get("/api/v1/portfolio/analytics/summary")
                        .header("Authorization", student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.domain").value("portfolio"))
                .andExpect(jsonPath("$.data.counters.certificates").value(1))
                .andExpect(jsonPath("$.data.counters.education").value(1))
                .andExpect(jsonPath("$.data.counters.items").value(1));
    }

    @Test
    void myPortfolio_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/portfolio/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicPortfolio_unknownHandle_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/portfolio/public/does-not-exist"))
                .andExpect(status().isNotFound());
    }
}
