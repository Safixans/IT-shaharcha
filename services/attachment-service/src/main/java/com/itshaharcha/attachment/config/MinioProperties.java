package com.itshaharcha.attachment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO/S3 settings. {@code endpoint} is what the service uses internally (put/stat/bucket);
 * {@code publicEndpoint} is the browser-reachable host used to SIGN download URLs — they must
 * resolve for the client, so in Docker this is the host-mapped address, not {@code minio:9000}.
 */
@ConfigurationProperties(prefix = "app.minio")
public record MinioProperties(
        String endpoint,
        String publicEndpoint,
        String accessKey,
        String secretKey,
        String bucket,
        int presignExpirySeconds) {

    public MinioProperties {
        if (publicEndpoint == null || publicEndpoint.isBlank()) {
            publicEndpoint = endpoint;
        }
        if (presignExpirySeconds <= 0) {
            presignExpirySeconds = 600;
        }
    }
}
