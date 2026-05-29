package com.itshaharcha.learning.service;

import com.itshaharcha.learning.dto.request.CourseInput;
import com.itshaharcha.learning.dto.request.LessonInput;
import com.itshaharcha.learning.dto.request.ModuleInput;
import com.itshaharcha.learning.dto.request.TrackInput;
import com.itshaharcha.learning.dto.response.CourseDetailResponse;
import com.itshaharcha.learning.dto.response.CourseResponse;
import com.itshaharcha.learning.dto.response.LessonResponse;
import com.itshaharcha.learning.dto.response.ModuleResponse;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.dto.response.TrackResponse;
import com.itshaharcha.learning.entity.Level;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CatalogService {

    PageResponse<TrackResponse> listTracks(String q, Pageable pageable);

    PageResponse<CourseResponse> listCourses(UUID trackId, Level level, Pageable pageable);

    CourseDetailResponse getCourse(UUID courseId);

    TrackResponse createTrack(TrackInput input);

    TrackResponse updateTrack(UUID trackId, TrackInput input);

    void deleteTrack(UUID trackId);

    CourseResponse createCourse(CourseInput input);

    CourseResponse updateCourse(UUID courseId, CourseInput input);

    void deleteCourse(UUID courseId);

    ModuleResponse createModule(ModuleInput input);

    ModuleResponse updateModule(UUID moduleId, ModuleInput input);

    void deleteModule(UUID moduleId);

    LessonResponse createLesson(LessonInput input);

    LessonResponse updateLesson(UUID lessonId, LessonInput input);

    void deleteLesson(UUID lessonId);
}
