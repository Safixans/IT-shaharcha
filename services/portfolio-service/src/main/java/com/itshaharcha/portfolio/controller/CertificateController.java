package com.itshaharcha.portfolio.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.portfolio.dto.request.CertificateCreate;
import com.itshaharcha.portfolio.dto.request.VerifyInput;
import com.itshaharcha.portfolio.dto.response.CertificateResponse;
import com.itshaharcha.portfolio.dto.response.PageResponse;
import com.itshaharcha.portfolio.service.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "certificates", description = "Certificate upload and verification")
@RestController
@RequestMapping("/api/v1/portfolio/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @Operation(summary = "List the caller's certificates")
    @GetMapping
    public ApiResponse<PageResponse<CertificateResponse>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(certificateService.list(pageable));
    }

    @Operation(summary = "Register a certificate (referencing an uploaded fileId)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CertificateResponse> create(@Valid @RequestBody CertificateCreate input) {
        return ApiResponse.ok(certificateService.create(input));
    }

    @Operation(summary = "Get a certificate")
    @GetMapping("/{certificateId}")
    public ApiResponse<CertificateResponse> get(@PathVariable UUID certificateId) {
        return ApiResponse.ok(certificateService.get(certificateId));
    }

    @Operation(summary = "Delete a certificate (owner)")
    @DeleteMapping("/{certificateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID certificateId) {
        certificateService.delete(certificateId);
    }

    @Operation(summary = "Verify a certificate (reviewer/admin)")
    @PostMapping("/{certificateId}:verify")
    @PreAuthorize("hasAuthority('CERTIFICATE_VERIFY')")
    public ApiResponse<CertificateResponse> verify(@PathVariable UUID certificateId,
                                                   @RequestBody(required = false) VerifyInput input) {
        return ApiResponse.ok(certificateService.verify(certificateId, input));
    }
}
