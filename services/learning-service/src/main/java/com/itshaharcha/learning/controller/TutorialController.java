package com.itshaharcha.learning.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.learning.dto.request.TutorialWatchedInput;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.dto.response.TutorialResponse;
import com.itshaharcha.learning.service.TutorialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "tutorials", description = "Tutorial videos")
@RestController
@RequestMapping("/api/v1/learning/tutorials")
@RequiredArgsConstructor
public class TutorialController {

    private final TutorialService tutorialService;

    @Operation(summary = "List tutorial videos")
    @GetMapping
    public ApiResponse<PageResponse<TutorialResponse>> listTutorials(
            @RequestParam(required = false) String topic,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(tutorialService.listTutorials(topic, pageable));
    }

    @Operation(summary = "Record tutorial watch progress")
    @PostMapping("/{tutorialId}:watched")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recordWatched(@PathVariable UUID tutorialId,
                              @Valid @RequestBody TutorialWatchedInput input) {
        tutorialService.recordWatched(tutorialId, input);
    }
}
