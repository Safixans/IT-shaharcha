package com.itshaharcha.attachment.integration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.SecretKey;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end against real Postgres + MinIO: upload → presigned download → fetch the bytes back.
 * Proves the storage round-trip and that the presigned URL is directly fetchable (range-capable,
 * so it streams audio on the client). Tokens are minted with the configured secret.
 */
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "app.security.jwt.secret=" + AttachmentFlowIntegrationTest.SECRET,
        "app.security.jwt.issuer=" + AttachmentFlowIntegrationTest.ISSUER,
        "app.minio.bucket=test-attachments"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AttachmentFlowIntegrationTest {

    static final String SECRET = "integration-test-secret-key-that-is-long-enough-32+";
    static final String ISSUER = "it-shaharcha-auth";

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("attachment_db")
            .withUsername("itsh")
            .withPassword("itsh_secret");

    @Container
    @SuppressWarnings("resource")
    static MinIOContainer minio = new MinIOContainer("minio/minio:RELEASE.2024-01-16T16-07-38Z");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.minio.endpoint", minio::getS3URL);
        registry.add("app.minio.public-endpoint", minio::getS3URL);
        registry.add("app.minio.access-key", minio::getUserName);
        registry.add("app.minio.secret-key", minio::getPassword);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private JsonMapper objectMapper;

    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private String bearer() {
        String token = Jwts.builder()
                .issuer(ISSUER)
                .subject(UUID.randomUUID().toString())
                .claim("type", "ACCESS")
                .claim("username", "tester")
                .claim("roles", List.of("ROLE_STUDENT"))
                .expiration(Date.from(Instant.now().plusSeconds(600)))
                .signWith(KEY)
                .compact();
        return "Bearer " + token;
    }

    @Test
    void upload_presign_fetchRoundTrip() throws Exception {
        byte[] content = "fake-audio-bytes-1234567890".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "lesson1.mp3", "audio/mpeg", content);

        MvcResult uploaded = mockMvc.perform(multipart("/api/upload")
                        .file(file)
                        .header("Authorization", bearer()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fileId").exists())
                .andExpect(jsonPath("$.data.contentType").value("audio/mpeg"))
                .andExpect(jsonPath("$.data.sizeBytes").value(content.length))
                .andReturn();
        UUID fileId = UUID.fromString(objectMapper
                .readTree(uploaded.getResponse().getContentAsString())
                .get("data").get("fileId").asString());

        MvcResult dl = mockMvc.perform(get("/api/download/" + fileId).header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").exists())
                .andExpect(jsonPath("$.data.expiresInSeconds").value(600))
                .andReturn();
        String url = objectMapper.readTree(dl.getResponse().getContentAsString())
                .get("data").get("url").asString();
        assertThat(url).contains("X-Amz-Signature");

        // The presigned URL is directly fetchable with no auth — exactly how a browser/audio element uses it.
        HttpResponse<byte[]> fetched = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray());
        assertThat(fetched.statusCode()).isEqualTo(200);
        assertThat(fetched.body()).isEqualTo(content);
    }

    @Test
    void upload_withoutToken_returns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "x.txt", "text/plain", "hi".getBytes());
        mockMvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void download_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/download/" + UUID.randomUUID()).header("Authorization", bearer()))
                .andExpect(status().isNotFound());
    }
}
