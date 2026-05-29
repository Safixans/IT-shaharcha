package com.itshaharcha.learning.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.learning.dto.request.TypingSessionInput;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.dto.response.TypingLessonResponse;
import com.itshaharcha.learning.dto.response.TypingSessionResponse;
import com.itshaharcha.learning.entity.Level;
import com.itshaharcha.learning.service.TypingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Tag(name = "typing", description = "Typing practice lessons and sessions")
@RestController
@RequestMapping("/api/v1/learning/typing")
@RequiredArgsConstructor
public class TypingController {

    private final TypingService typingService;

    @Operation(summary = "List typing practice lessons")
    @GetMapping("/lessons")
    public ApiResponse<PageResponse<TypingLessonResponse>> listTypingLessons(
            @RequestParam(required = false) Level difficulty,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(typingService.listTypingLessons(difficulty, pageable));
    }

    @Operation(summary = "Submit a completed typing session")
    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TypingSessionResponse> submitSession(
            @Valid @RequestBody TypingSessionInput input) {
        return ApiResponse.ok(typingService.submitSession(input));
    }

    @Operation(summary = "List the caller's typing sessions")
    @GetMapping("/sessions")
    public ApiResponse<PageResponse<TypingSessionResponse>> listMySessions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(typingService.listMySessions(from, to, pageable));
    }
}
