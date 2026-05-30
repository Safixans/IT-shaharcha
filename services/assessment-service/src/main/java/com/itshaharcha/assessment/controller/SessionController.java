package com.itshaharcha.assessment.controller;

import com.itshaharcha.assessment.dto.request.SaveAnswersInput;
import com.itshaharcha.assessment.dto.request.SubmitInput;
import com.itshaharcha.assessment.dto.response.ExamResultResponse;
import com.itshaharcha.assessment.dto.response.ExamSessionResponse;
import com.itshaharcha.assessment.dto.response.SessionQuestionResponse;
import com.itshaharcha.assessment.service.SessionService;
import com.itshaharcha.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "sessions", description = "Exam sessions — start, answer, submit")
@RestController
@RequestMapping("/api/v1/assessment")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @Operation(summary = "Start (or resume) an exam session")
    @PostMapping("/exams/{examId}:start")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ExamSessionResponse> start(@PathVariable UUID examId) {
        return ApiResponse.ok(sessionService.start(examId));
    }

    @Operation(summary = "Get a session (caller's own)")
    @GetMapping("/sessions/{sessionId}")
    public ApiResponse<ExamSessionResponse> getSession(@PathVariable UUID sessionId) {
        return ApiResponse.ok(sessionService.getSession(sessionId));
    }

    @Operation(summary = "List the session's questions (answers withheld)")
    @GetMapping("/sessions/{sessionId}/questions")
    public ApiResponse<List<SessionQuestionResponse>> getSessionQuestions(@PathVariable UUID sessionId) {
        return ApiResponse.ok(sessionService.getSessionQuestions(sessionId));
    }

    @Operation(summary = "Save/replace answers for the session")
    @PutMapping("/sessions/{sessionId}/answers")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveAnswers(@PathVariable UUID sessionId, @Valid @RequestBody SaveAnswersInput input) {
        sessionService.saveAnswers(sessionId, input);
    }

    @Operation(summary = "Submit the session for synchronous scoring")
    @PostMapping("/sessions/{sessionId}:submit")
    public ApiResponse<ExamResultResponse> submit(@PathVariable UUID sessionId,
                                                  @Valid @RequestBody(required = false) SubmitInput input) {
        return ApiResponse.ok(sessionService.submit(sessionId, input));
    }
}
