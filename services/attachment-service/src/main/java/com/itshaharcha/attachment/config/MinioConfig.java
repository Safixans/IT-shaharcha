package com.itshaharcha.attachment.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MinioConfig {

    /** Internal client — put / stat / bucket ops, reaches MinIO on the service network. */
    @Bean
    public MinioClient minioClient(MinioProperties props) {
        return MinioClient.builder()
                .endpoint(props.endpoint())
                .credentials(props.accessKey(), props.secretKey())
                .build();
    }

    /** Presign client — signs download URLs against the browser-reachable public endpoint. */
    @Bean
    public MinioClient presignClient(MinioProperties props) {
        return MinioClient.builder()
                .endpoint(props.publicEndpoint())
                .credentials(props.accessKey(), props.secretKey())
                .build();
    }

    /** Ensure the bucket exists at startup (fail-fast if MinIO is unreachable). */
    @Bean
    public ApplicationRunner ensureBucket(MinioClient minioClient, MinioProperties props) {
        return args -> {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(props.bucket()).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(props.bucket()).build());
                log.info("Created MinIO bucket '{}'", props.bucket());
            }
        };
    }
}
