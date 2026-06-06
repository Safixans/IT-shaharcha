package com.itshaharcha.attachment.controller;

import com.itshaharcha.attachment.dto.AttachmentRef;
import com.itshaharcha.attachment.security.SecurityUtils;
import com.itshaharcha.attachment.service.AttachmentService;
import com.itshaharcha.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "upload", description = "Upload file blobs to object storage")
@RestController
@RequiredArgsConstructor
public class UploadController {

    private final AttachmentService attachmentService;

    @Operation(summary = "Upload a file; returns a fileId other services reference")
    @PostMapping(value = "/api/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AttachmentRef> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(attachmentService.upload(file, SecurityUtils.currentAccountId()));
    }
}
