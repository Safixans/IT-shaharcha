package com.itshaharcha.attachment.service;

import com.itshaharcha.attachment.config.MinioProperties;
import com.itshaharcha.common.exception.ApplicationException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/** Thin MinIO wrapper: store objects with the internal client, sign URLs with the public one. */
@Slf4j
@Service
public class StorageService {

    private final MinioClient minioClient;
    private final MinioClient presignClient;
    private final MinioProperties props;

    public StorageService(MinioClient minioClient, MinioClient presignClient, MinioProperties props) {
        this.minioClient = minioClient;
        this.presignClient = presignClient;
        this.props = props;
    }

    public void put(String objectKey, InputStream data, long size, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(props.bucket())
                    .object(objectKey)
                    .stream(data, size, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception ex) {
            log.error("Failed to store object {}: {}", objectKey, ex.getMessage());
            throw new ApplicationException(
                    com.itshaharcha.common.exception.ErrorCode.INTERNAL_ERROR, "Could not store file");
        }
    }

    /** Presigned GET URL (range-capable → streams audio), valid for {@code expirySeconds}. */
    public String presignedGet(String objectKey, int expirySeconds) {
        try {
            return presignClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(props.bucket())
                    .object(objectKey)
                    .expiry(expirySeconds)
                    .build());
        } catch (Exception ex) {
            log.error("Failed to presign object {}: {}", objectKey, ex.getMessage());
            throw new ApplicationException(
                    com.itshaharcha.common.exception.ErrorCode.INTERNAL_ERROR, "Could not create download link");
        }
    }

    public void remove(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(props.bucket())
                    .object(objectKey)
                    .build());
        } catch (Exception ex) {
            log.warn("Failed to remove object {}: {}", objectKey, ex.getMessage());
        }
    }
}
