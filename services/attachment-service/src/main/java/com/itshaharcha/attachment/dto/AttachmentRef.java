package com.itshaharcha.attachment.dto;

import java.util.UUID;

/** Returned on upload; the {@code fileId} is what other services persist and reference. */
public record AttachmentRef(
        UUID fileId,
        String originalName,
        String contentType,
        long sizeBytes) {
}
