package com.itshaharcha.attachment.service;

import com.itshaharcha.attachment.dto.AttachmentRef;
import com.itshaharcha.attachment.dto.DownloadUrl;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface AttachmentService {

    AttachmentRef upload(MultipartFile file, UUID ownerAccountId);

    DownloadUrl download(UUID fileId);
}
