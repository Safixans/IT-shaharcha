package com.itshaharcha.attachment.controller;

import com.itshaharcha.attachment.dto.DownloadUrl;
import com.itshaharcha.attachment.service.AttachmentService;
import com.itshaharcha.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "download", description = "Presigned download URLs (range-capable; stream audio)")
@RestController
@RequestMapping("/api/download")
@RequiredArgsConstructor
public class DownloadController {

    private final AttachmentService attachmentService;

    @Operation(summary = "Get a time-limited presigned URL for a file (set it as the media src)")
    @GetMapping("/{fileId}")
    public ApiResponse<DownloadUrl> download(@PathVariable UUID fileId) {
        return ApiResponse.ok(attachmentService.download(fileId));
    }
}
