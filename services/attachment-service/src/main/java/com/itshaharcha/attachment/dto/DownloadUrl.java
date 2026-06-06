package com.itshaharcha.attachment.dto;

import java.util.UUID;

/**
 * A time-limited, browser-fetchable URL for an object. The presigned URL is self-authenticating
 * (no JWT needed) and range-capable, so it works directly as an {@code <audio>}/{@code <img>} src
 * and streams audio. Set it as the media source on the client.
 */
public record DownloadUrl(
        UUID fileId,
        String url,
        long expiresInSeconds,
        String contentType,
        String originalName,
        long sizeBytes) {
}
