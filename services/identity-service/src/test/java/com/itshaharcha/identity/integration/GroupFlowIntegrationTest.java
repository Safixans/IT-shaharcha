package com.itshaharcha.identity.integration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.itshaharcha.identity.entity.Account;
import com.itshaharcha.identity.entity.AccountStatus;
import com.itshaharcha.identity.entity.Role;
import com.itshaharcha.identity.repository.AccountRepository;
import com.itshaharcha.identity.repository.RoleRepository;
import com.itshaharcha.identity.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end of the groups / teacher↔student domain against a real Postgres. Accounts are
 * seeded directly (roles come from Flyway) and tokens minted with the configured secret, so
 * the flow exercises the actual endpoints, the one-group-per-student rule, and ownership authz.
 */
@SpringBootTest(properties = {
        "app.events.kafka-enabled=false",
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration,"
                + "org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration",
        "app.security.jwt.secret=integration-test-secret-key-that-is-long-enough-32+"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class GroupFlowIntegrationTest {

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
    @Autowired private AccountRepository accountRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private JwtTokenProvider jwt;

    private Account seed(String username, String roleName) {
        Role role = roleRepository.findByName(roleName).orElseThrow();
        Account a = new Account();
        a.setEmail(username + "@itsh.dev");
        a.setUsername(username);
        a.setStatus(AccountStatus.ACTIVE);
        a.setEmailVerified(true);
        a.setRoles(Set.of(role));
        return accountRepository.save(a);
    }

    private String bearer(Account account, String role) {
        return "Bearer " + jwt.generateAccessToken(account, Set.of(role));
    }

    @Test
    void create_addStudent_oneGroupRule_roster_andOwnershipAuthz() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Account admin = seed("admin-" + suffix, "ROLE_ADMIN");
        Account teacher = seed("teacher-" + suffix, "ROLE_TEACHER");
        Account teacher2 = seed("teacher2-" + suffix, "ROLE_TEACHER");
        Account student = seed("student-" + suffix, "ROLE_STUDENT");
        Account student2 = seed("student2-" + suffix, "ROLE_STUDENT");

        String adminTok = bearer(admin, "ROLE_ADMIN");
        String teacherTok = bearer(teacher, "ROLE_TEACHER");
        String teacher2Tok = bearer(teacher2, "ROLE_TEACHER");
        String studentTok = bearer(student, "ROLE_STUDENT");

        // ---- admin creates a group owned by the teacher ----
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "name", "Group " + suffix, "teacherId", teacher.getId().toString()));
        MvcResult created = mockMvc.perform(post("/api/v1/identity/groups")
                        .header("Authorization", adminTok)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.teacherId").value(teacher.getId().toString()))
                .andExpect(jsonPath("$.data.studentCount").value(0))
                .andReturn();
        UUID groupId = UUID.fromString(objectMapper
                .readTree(created.getResponse().getContentAsString()).get("data").get("id").asString());

        // ---- a student cannot create a group (not privileged) ----
        mockMvc.perform(post("/api/v1/identity/groups")
                        .header("Authorization", studentTok)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        // ---- the owning teacher adds the student ----
        String addBody = objectMapper.writeValueAsString(java.util.Map.of(
                "studentId", student.getId().toString()));
        mockMvc.perform(post("/api/v1/identity/groups/" + groupId + "/students")
                        .header("Authorization", teacherTok)
                        .contentType(MediaType.APPLICATION_JSON).content(addBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.studentId").value(student.getId().toString()));

        // ---- one group per student: re-adding → 409 ----
        mockMvc.perform(post("/api/v1/identity/groups/" + groupId + "/students")
                        .header("Authorization", teacherTok)
                        .contentType(MediaType.APPLICATION_JSON).content(addBody))
                .andExpect(status().isConflict());

        // ---- adding a non-student (a teacher) → 400 ----
        String addTeacherAsStudent = objectMapper.writeValueAsString(java.util.Map.of(
                "studentId", teacher2.getId().toString()));
        mockMvc.perform(post("/api/v1/identity/groups/" + groupId + "/students")
                        .header("Authorization", teacherTok)
                        .contentType(MediaType.APPLICATION_JSON).content(addTeacherAsStudent))
                .andExpect(status().isBadRequest());

        // ---- a different teacher cannot manage this group → 403 ----
        String addStudent2 = objectMapper.writeValueAsString(java.util.Map.of(
                "studentId", student2.getId().toString()));
        mockMvc.perform(post("/api/v1/identity/groups/" + groupId + "/students")
                        .header("Authorization", teacher2Tok)
                        .contentType(MediaType.APPLICATION_JSON).content(addStudent2))
                .andExpect(status().isForbidden());

        // ---- teacher roster + grading authz check ----
        mockMvc.perform(get("/api/v1/identity/me/students").header("Authorization", teacherTok))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].studentId").value(student.getId().toString()));

        mockMvc.perform(get("/api/v1/identity/me/students/" + student.getId())
                        .header("Authorization", teacherTok))
                .andExpect(status().isOk());

        // a student who isn't mine → 404 (the grading-authorization negative case)
        mockMvc.perform(get("/api/v1/identity/me/students/" + student2.getId())
                        .header("Authorization", teacherTok))
                .andExpect(status().isNotFound());

        // ---- remove the student, freeing them for another group ----
        mockMvc.perform(delete("/api/v1/identity/groups/" + groupId + "/students/" + student.getId())
                        .header("Authorization", teacherTok))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/identity/groups/" + groupId + "/students")
                        .header("Authorization", teacherTok)
                        .contentType(MediaType.APPLICATION_JSON).content(addBody))
                .andExpect(status().isCreated());
    }
}
