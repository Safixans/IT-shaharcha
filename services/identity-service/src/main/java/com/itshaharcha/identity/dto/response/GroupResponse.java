package com.itshaharcha.identity.dto.response;

import java.time.Instant;
import java.util.UUID;

public record GroupResponse(
        UUID id,
        String name,
        UUID teacherId,
        String teacherUsername,
        int studentCount,
        Instant createdAt) {
}
