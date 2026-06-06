package com.itshaharcha.attachment.service.impl;

import com.itshaharcha.attachment.config.MinioProperties;
import com.itshaharcha.attachment.dto.AttachmentRef;
import com.itshaharcha.attachment.dto.DownloadUrl;
import com.itshaharcha.attachment.entity.Attachment;
import com.itshaharcha.attachment.repository.AttachmentRepository;
import com.itshaharcha.attachment.service.AttachmentService;
import com.itshaharcha.attachment.service.StorageService;
import com.itshaharcha.common.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final AttachmentRepository attachmentRepository;
    private final StorageService storage;
    private final MinioProperties props;

    @Override
    @Transactional
    public AttachmentRef upload(MultipartFile file, UUID ownerAccountId) {
        if (file == null || file.isEmpty()) {
            throw ApplicationException.badRequest("File must not be empty");
        }
        String contentType = file.getContentType() != null ? file.getContentType() : DEFAULT_CONTENT_TYPE;
        String safeName = sanitize(file.getOriginalFilename());
        String objectKey = UUID.randomUUID() + (safeName.isBlank() ? "" : "/" + safeName);

        try {
            storage.put(objectKey, file.getInputStream(), file.getSize(), contentType);
        } catch (IOException ex) {
            throw ApplicationException.badRequest("Could not read uploaded file");
        }

        Attachment attachment = new Attachment();
        attachment.setOwnerAccountId(ownerAccountId);
        attachment.setObjectKey(objectKey);
        attachment.setOriginalName(safeName.isBlank() ? null : safeName);
        attachment.setContentType(contentType);
        attachment.setSizeBytes(file.getSize());
        Attachment saved = attachmentRepository.save(attachment);
        log.info("Stored attachment {} ({} bytes) for {}", saved.getId(), file.getSize(), ownerAccountId);

        return new AttachmentRef(saved.getId(), saved.getOriginalName(),
                saved.getContentType(), saved.getSizeBytes());
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadUrl download(UUID fileId) {
        Attachment a = attachmentRepository.findById(fileId)
                .orElseThrow(() -> ApplicationException.notFound("Attachment not found"));
        int ttl = props.presignExpirySeconds();
        String url = storage.presignedGet(a.getObjectKey(), ttl);
        return new DownloadUrl(a.getId(), url, ttl, a.getContentType(), a.getOriginalName(), a.getSizeBytes());
    }

    /** Drop any path components from the client-supplied filename. */
    private String sanitize(String name) {
        if (name == null) {
            return "";
        }
        String base = name.replace("\\", "/");
        int slash = base.lastIndexOf('/');
        return (slash >= 0 ? base.substring(slash + 1) : base).trim();
    }
}
