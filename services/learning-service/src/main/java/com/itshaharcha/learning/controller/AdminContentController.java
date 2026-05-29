package com.itshaharcha.learning.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.learning.dto.request.DocInput;
import com.itshaharcha.learning.dto.request.TutorialInput;
import com.itshaharcha.learning.dto.request.TypingLessonInput;
import com.itshaharcha.learning.dto.response.DocResponse;
import com.itshaharcha.learning.dto.response.TutorialResponse;
import com.itshaharcha.learning.dto.response.TypingLessonResponse;
import com.itshaharcha.learning.service.DocService;
import com.itshaharcha.learning.service.TutorialService;
import com.itshaharcha.learning.service.TypingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "admin-content", description = "Authoring: tutorials, docs, typing lessons")
@RestController
@RequestMapping("/api/v1/learning/admin")
@RequiredArgsConstructor
public class AdminContentController {

    private final TutorialService tutorialService;
    private final DocService docService;
    private final TypingService typingService;

    @Operation(summary = "Add a tutorial video")
    @PostMapping("/tutorials")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('TUTORIAL_WRITE')")
    public ApiResponse<TutorialResponse> createTutorial(@Valid @RequestBody TutorialInput input) {
        return ApiResponse.ok(tutorialService.createTutorial(input));
    }

    @Operation(summary = "Update a tutorial")
    @PatchMapping("/tutorials/{tutorialId}")
    @PreAuthorize("hasAuthority('TUTORIAL_EDIT')")
    public ApiResponse<TutorialResponse> updateTutorial(@PathVariable UUID tutorialId,
                                                        @Valid @RequestBody TutorialInput input) {
        return ApiResponse.ok(tutorialService.updateTutorial(tutorialId, input));
    }

    @Operation(summary = "Delete a tutorial")
    @DeleteMapping("/tutorials/{tutorialId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('TUTORIAL_DELETE')")
    public void deleteTutorial(@PathVariable UUID tutorialId) {
        tutorialService.deleteTutorial(tutorialId);
    }

    @Operation(summary = "Add a documentation / reading entry")
    @PostMapping("/docs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    public ApiResponse<DocResponse> createDoc(@Valid @RequestBody DocInput input) {
        return ApiResponse.ok(docService.createDoc(input));
    }

    @Operation(summary = "Update a doc")
    @PatchMapping("/docs/{docId}")
    @PreAuthorize("hasAuthority('DOC_EDIT')")
    public ApiResponse<DocResponse> updateDoc(@PathVariable UUID docId,
                                              @Valid @RequestBody DocInput input) {
        return ApiResponse.ok(docService.updateDoc(docId, input));
    }

    @Operation(summary = "Delete a doc")
    @DeleteMapping("/docs/{docId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('DOC_DELETE')")
    public void deleteDoc(@PathVariable UUID docId) {
        docService.deleteDoc(docId);
    }

    @Operation(summary = "Add a typing practice lesson")
    @PostMapping("/typing/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('TYPING_WRITE')")
    public ApiResponse<TypingLessonResponse> createTypingLesson(
            @Valid @RequestBody TypingLessonInput input) {
        return ApiResponse.ok(typingService.createTypingLesson(input));
    }

    @Operation(summary = "Update a typing lesson")
    @PatchMapping("/typing/lessons/{lessonId}")
    @PreAuthorize("hasAuthority('TYPING_EDIT')")
    public ApiResponse<TypingLessonResponse> updateTypingLesson(
            @PathVariable UUID lessonId, @Valid @RequestBody TypingLessonInput input) {
        return ApiResponse.ok(typingService.updateTypingLesson(lessonId, input));
    }

    @Operation(summary = "Delete a typing lesson")
    @DeleteMapping("/typing/lessons/{lessonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('TYPING_DELETE')")
    public void deleteTypingLesson(@PathVariable UUID lessonId) {
        typingService.deleteTypingLesson(lessonId);
    }
}
