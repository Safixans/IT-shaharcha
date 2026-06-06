package com.itshaharcha.assessment.client;

import com.itshaharcha.common.exception.ApplicationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.UUID;

/**
 * Talks to identity-service for teacher↔student authorization, forwarding the caller's bearer
 * token so identity resolves "me" as the current teacher. Used by grading (queue + grade-writing).
 */
@Component
public class IdentityClient {

    private final RestClient restClient;

    public IdentityClient(IdentityProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.baseUrl()).build();
    }

    /** The current teacher's student ids across their groups. */
    public List<UUID> myStudentIds() {
        ApiEnvelope<List<MemberResponse>> body = restClient.get()
                .uri("/api/v1/identity/me/students")
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {
                });
        if (body == null || body.data() == null) {
            return List.of();
        }
        return body.data().stream().map(MemberResponse::studentId).toList();
    }

    /** True iff {@code studentId} belongs to the current teacher (identity returns 200 vs 404). */
    public boolean isMyStudent(UUID studentId) {
        return restClient.get()
                .uri("/api/v1/identity/me/students/{id}", studentId)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .exchange((req, res) -> res.getStatusCode().is2xxSuccessful());
    }

    private String bearer() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            String header = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null && !header.isBlank()) {
                return header;
            }
        }
        throw ApplicationException.unauthorized("Missing bearer token for identity call");
    }

    /** Minimal mirrors of identity's response envelope + member DTO. */
    record ApiEnvelope<T>(boolean success, String message, T data) {
    }

    record MemberResponse(UUID studentId, String username, String email, UUID groupId) {
    }
}
