package com.itshaharcha.learning.service;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.exception.ErrorCode;
import com.itshaharcha.learning.dto.request.ModuleInput;
import com.itshaharcha.learning.dto.request.TrackInput;
import com.itshaharcha.learning.dto.response.CourseDetailResponse;
import com.itshaharcha.learning.dto.response.ModuleResponse;
import com.itshaharcha.learning.entity.Course;
import com.itshaharcha.learning.entity.Lesson;
import com.itshaharcha.learning.entity.Module;
import com.itshaharcha.learning.entity.Track;
import com.itshaharcha.learning.mapper.LearningMapper;
import com.itshaharcha.learning.repository.CourseRepository;
import com.itshaharcha.learning.repository.LessonRepository;
import com.itshaharcha.learning.repository.ModuleRepository;
import com.itshaharcha.learning.repository.TrackRepository;
import com.itshaharcha.learning.service.impl.CatalogServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceImplTest {

    @Mock private TrackRepository trackRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private ModuleRepository moduleRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private LearningMapper mapper;
    @InjectMocks private CatalogServiceImpl service;

    @Test
    void createTrack_persistsAndReturnsZeroCourseCount() {
        TrackInput input = new TrackInput("Java", "java", "Learn Java");
        when(trackRepository.save(any(Track.class))).thenAnswer(i -> i.getArgument(0));

        service.createTrack(input);

        verify(mapper).toTrackResponse(any(Track.class), anyInt());
        verify(trackRepository).save(any(Track.class));
    }

    @Test
    void deleteTrack_withCourses_throwsConflict() {
        UUID trackId = UUID.randomUUID();
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(new Track()));
        when(courseRepository.countByTrackId(trackId)).thenReturn(3L);

        assertThatThrownBy(() -> service.deleteTrack(trackId))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CONFLICT);

        verify(trackRepository, never()).delete(any());
    }

    @Test
    void deleteTrack_empty_deletes() {
        UUID trackId = UUID.randomUUID();
        Track track = new Track();
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));
        when(courseRepository.countByTrackId(trackId)).thenReturn(0L);

        service.deleteTrack(trackId);

        verify(trackRepository).delete(track);
    }

    @Test
    void createModule_requiresExistingCourse() {
        UUID courseId = UUID.randomUUID();
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createModule(new ModuleInput(courseId, "Intro", 0)))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void getCourse_assemblesModulesWithLessons() {
        UUID courseId = UUID.randomUUID();
        Course course = new Course();
        Module module = new Module();
        module.setCourseId(courseId);
        UUID moduleId = UUID.randomUUID();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId)).thenReturn(List.of(module));
        when(lessonRepository.findByModuleIdOrderByOrderIndexAsc(any())).thenReturn(List.of(new Lesson()));
        when(lessonRepository.countByCourseId(courseId)).thenReturn(1L);
        when(mapper.toModuleResponse(any(Module.class), any())).thenReturn(mockModule());
        CourseDetailResponse detail = mockDetail();
        when(mapper.toCourseDetail(any(), anyInt(), any())).thenReturn(detail);

        assertThat(service.getCourse(courseId)).isSameAs(detail);
        verify(mapper).toCourseDetail(course, 1, List.of(mockModule()));
    }

    private ModuleResponse mockModule() {
        return new ModuleResponse(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "M", 0, List.of());
    }

    private CourseDetailResponse mockDetail() {
        return new CourseDetailResponse(UUID.randomUUID(), null, "C", null, null, null, 1, null, List.of());
    }
}
