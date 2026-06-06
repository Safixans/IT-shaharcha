package com.itshaharcha.identity.dto.response;

import java.util.UUID;

public record MemberResponse(
        UUID studentId,
        String username,
        String email,
        UUID groupId) {
}
