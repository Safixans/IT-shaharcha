package com.itshaharcha.learning.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.learning.dto.request.CourseInput;
import com.itshaharcha.learning.dto.request.LessonInput;
import com.itshaharcha.learning.dto.request.ModuleInput;
import com.itshaharcha.learning.dto.request.TrackInput;
import com.itshaharcha.learning.dto.response.CourseResponse;
import com.itshaharcha.learning.dto.response.LessonResponse;
import com.itshaharcha.learning.dto.response.ModuleResponse;
import com.itshaharcha.learning.dto.response.TrackResponse;
import com.itshaharcha.learning.service.CatalogService;
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

@Tag(name = "admin-catalog", description = "Authoring: tracks, courses, modules, lessons")
@RestController
@RequestMapping("/api/v1/learning/admin")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final CatalogService catalogService;

    @Operation(summary = "Create a skill track")
    @PostMapping("/tracks")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('TRACK_WRITE')")
    public ApiResponse<TrackResponse> createTrack(@Valid @RequestBody TrackInput input) {
        return ApiResponse.ok(catalogService.createTrack(input));
    }

    @Operation(summary = "Update a track")
    @PatchMapping("/tracks/{trackId}")
    @PreAuthorize("hasAuthority('TRACK_EDIT')")
    public ApiResponse<TrackResponse> updateTrack(@PathVariable UUID trackId,
                                                  @Valid @RequestBody TrackInput input) {
        return ApiResponse.ok(catalogService.updateTrack(trackId, input));
    }

    @Operation(summary = "Delete a track")
    @DeleteMapping("/tracks/{trackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('TRACK_DELETE')")
    public void deleteTrack(@PathVariable UUID trackId) {
        catalogService.deleteTrack(trackId);
    }

    @Operation(summary = "Create a course")
    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('COURSE_WRITE')")
    public ApiResponse<CourseResponse> createCourse(@Valid @RequestBody CourseInput input) {
        return ApiResponse.ok(catalogService.createCourse(input));
    }

    @Operation(summary = "Update a course")
    @PatchMapping("/courses/{courseId}")
    @PreAuthorize("hasAuthority('COURSE_EDIT')")
    public ApiResponse<CourseResponse> updateCourse(@PathVariable UUID courseId,
                                                    @Valid @RequestBody CourseInput input) {
        return ApiResponse.ok(catalogService.updateCourse(courseId, input));
    }

    @Operation(summary = "Delete a course")
    @DeleteMapping("/courses/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('COURSE_DELETE')")
    public void deleteCourse(@PathVariable UUID courseId) {
        catalogService.deleteCourse(courseId);
    }

    @Operation(summary = "Create a module within a course")
    @PostMapping("/modules")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MODULE_WRITE')")
    public ApiResponse<ModuleResponse> createModule(@Valid @RequestBody ModuleInput input) {
        return ApiResponse.ok(catalogService.createModule(input));
    }

    @Operation(summary = "Update a module")
    @PatchMapping("/modules/{moduleId}")
    @PreAuthorize("hasAuthority('MODULE_EDIT')")
    public ApiResponse<ModuleResponse> updateModule(@PathVariable UUID moduleId,
                                                    @Valid @RequestBody ModuleInput input) {
        return ApiResponse.ok(catalogService.updateModule(moduleId, input));
    }

    @Operation(summary = "Delete a module")
    @DeleteMapping("/modules/{moduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MODULE_DELETE')")
    public void deleteModule(@PathVariable UUID moduleId) {
        catalogService.deleteModule(moduleId);
    }

    @Operation(summary = "Create a lesson within a module")
    @PostMapping("/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('LESSON_WRITE')")
    public ApiResponse<LessonResponse> createLesson(@Valid @RequestBody LessonInput input) {
        return ApiResponse.ok(catalogService.createLesson(input));
    }

    @Operation(summary = "Update a lesson")
    @PatchMapping("/lessons/{lessonId}")
    @PreAuthorize("hasAuthority('LESSON_EDIT')")
    public ApiResponse<LessonResponse> updateLesson(@PathVariable UUID lessonId,
                                                    @Valid @RequestBody LessonInput input) {
        return ApiResponse.ok(catalogService.updateLesson(lessonId, input));
    }

    @Operation(summary = "Delete a lesson")
    @DeleteMapping("/lessons/{lessonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('LESSON_DELETE')")
    public void deleteLesson(@PathVariable UUID lessonId) {
        catalogService.deleteLesson(lessonId);
    }
}
