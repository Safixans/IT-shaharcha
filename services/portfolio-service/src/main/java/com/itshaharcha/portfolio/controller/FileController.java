package com.itshaharcha.portfolio.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.portfolio.dto.response.FileRef;
import com.itshaharcha.portfolio.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "files", description = "Uploaded file blobs")
@RestController
@RequestMapping("/api/v1/portfolio/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "Upload a file blob (PDF/image); returns a fileId to reference")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FileRef> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(fileService.store(file));
    }
}
