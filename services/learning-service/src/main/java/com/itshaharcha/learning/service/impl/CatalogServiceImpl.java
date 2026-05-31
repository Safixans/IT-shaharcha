package com.itshaharcha.learning.service.impl;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.learning.dto.request.CourseInput;
import com.itshaharcha.learning.dto.request.LessonInput;
import com.itshaharcha.learning.dto.request.ModuleInput;
import com.itshaharcha.learning.dto.request.TrackInput;
import com.itshaharcha.learning.dto.response.CourseDetailResponse;
import com.itshaharcha.learning.dto.response.CourseResponse;
import com.itshaharcha.learning.dto.response.LessonDetailResponse;
import com.itshaharcha.learning.dto.response.LessonResponse;
import com.itshaharcha.learning.dto.response.ModuleResponse;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.dto.response.TrackResponse;
import com.itshaharcha.learning.entity.Course;
import com.itshaharcha.learning.entity.Lesson;
import com.itshaharcha.learning.entity.Level;
import com.itshaharcha.learning.entity.Module;
import com.itshaharcha.learning.entity.Track;
import com.itshaharcha.learning.mapper.LearningMapper;
import com.itshaharcha.learning.repository.CourseRepository;
import com.itshaharcha.learning.repository.LessonRepository;
import com.itshaharcha.learning.repository.ModuleRepository;
import com.itshaharcha.learning.repository.TrackRepository;
import com.itshaharcha.learning.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final TrackRepository trackRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final LearningMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TrackResponse> listTracks(String q, Pageable pageable) {
        Page<Track> page = StringUtils.hasText(q)
                ? trackRepository.findByTitleContainingIgnoreCase(q, pageable)
                : trackRepository.findAll(pageable);
        return PageResponse.from(page, t ->
                mapper.toTrackResponse(t, (int) courseRepository.countByTrackId(t.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> listCourses(UUID trackId, Level level, Pageable pageable) {
        Page<Course> page = courseRepository.search(trackId, level, pageable);
        return PageResponse.from(page, c ->
                mapper.toCourseResponse(c, (int) lessonRepository.countByCourseId(c.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailResponse getCourse(UUID courseId) {
        Course course = requireCourse(courseId);
        List<ModuleResponse> modules = moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId).stream()
                .map(m -> mapper.toModuleResponse(m, lessonsOf(m.getId())))
                .toList();
        int lessonCount = (int) lessonRepository.countByCourseId(courseId);
        return mapper.toCourseDetail(course, lessonCount, modules);
    }

    @Override
    @Transactional(readOnly = true)
    public LessonDetailResponse getLesson(UUID lessonId) {
        return mapper.toLessonDetail(requireLesson(lessonId));
    }

    private List<LessonResponse> lessonsOf(UUID moduleId) {
        return lessonRepository.findByModuleIdOrderByOrderIndexAsc(moduleId).stream()
                .map(mapper::toLessonResponse)
                .toList();
    }

    @Override
    @Transactional
    public TrackResponse createTrack(TrackInput input) {
        Track track = new Track();
        apply(track, input);
        Track saved = trackRepository.save(track);
        return mapper.toTrackResponse(saved, 0);
    }

    @Override
    @Transactional
    public TrackResponse updateTrack(UUID trackId, TrackInput input) {
        Track track = requireTrack(trackId);
        apply(track, input);
        Track saved = trackRepository.save(track);
        return mapper.toTrackResponse(saved, (int) courseRepository.countByTrackId(trackId));
    }

    @Override
    @Transactional
    public void deleteTrack(UUID trackId) {
        Track track = requireTrack(trackId);
        if (courseRepository.countByTrackId(trackId) > 0) {
            throw ApplicationException.conflict("Track has courses and cannot be deleted");
        }
        trackRepository.delete(track);
    }

    @Override
    @Transactional
    public CourseResponse createCourse(CourseInput input) {
        Course course = new Course();
        apply(course, input);
        Course saved = courseRepository.save(course);
        return mapper.toCourseResponse(saved, 0);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(UUID courseId, CourseInput input) {
        Course course = requireCourse(courseId);
        apply(course, input);
        Course saved = courseRepository.save(course);
        return mapper.toCourseResponse(saved, (int) lessonRepository.countByCourseId(courseId));
    }

    @Override
    @Transactional
    public void deleteCourse(UUID courseId) {
        courseRepository.delete(requireCourse(courseId));
    }

    @Override
    @Transactional
    public ModuleResponse createModule(ModuleInput input) {
        requireCourse(input.courseId());
        Module module = new Module();
        module.setCourseId(input.courseId());
        module.setTitle(input.title());
        module.setOrderIndex(orderOrDefault(input.order()));
        return mapper.toModuleResponse(moduleRepository.save(module), List.of());
    }

    @Override
    @Transactional
    public ModuleResponse updateModule(UUID moduleId, ModuleInput input) {
        Module module = requireModule(moduleId);
        module.setTitle(input.title());
        if (input.order() != null) {
            module.setOrderIndex(input.order());
        }
        Module saved = moduleRepository.save(module);
        return mapper.toModuleResponse(saved, lessonsOf(moduleId));
    }

    @Override
    @Transactional
    public void deleteModule(UUID moduleId) {
        moduleRepository.delete(requireModule(moduleId));
    }

    @Override
    @Transactional
    public LessonResponse createLesson(LessonInput input) {
        requireModule(input.moduleId());
        Lesson lesson = new Lesson();
        lesson.setModuleId(input.moduleId());
        applyLessonFields(lesson, input);
        lesson.setOrderIndex(orderOrDefault(input.order()));
        return mapper.toLessonResponse(lessonRepository.save(lesson));
    }

    @Override
    @Transactional
    public LessonResponse updateLesson(UUID lessonId, LessonInput input) {
        Lesson lesson = requireLesson(lessonId);
        applyLessonFields(lesson, input);
        if (input.order() != null) {
            lesson.setOrderIndex(input.order());
        }
        return mapper.toLessonResponse(lessonRepository.save(lesson));
    }

    @Override
    @Transactional
    public void deleteLesson(UUID lessonId) {
        lessonRepository.delete(requireLesson(lessonId));
    }

    private void applyLessonFields(Lesson lesson, LessonInput input) {
        lesson.setTitle(input.title());
        if (input.kind() != null) {
            lesson.setKind(input.kind());
        }
        lesson.setEstimatedMinutes(input.estimatedMinutes());
        lesson.setBody(input.body());
    }

    private void apply(Track track, TrackInput input) {
        track.setTitle(input.title());
        track.setSlug(input.slug());
        track.setDescription(input.description());
    }

    private void apply(Course course, CourseInput input) {
        course.setTrackId(input.trackId());
        course.setTitle(input.title());
        course.setSlug(input.slug());
        course.setSummary(input.summary());
        course.setLevel(input.level());
        course.setEstimatedMinutes(input.estimatedMinutes());
    }

    private int orderOrDefault(Integer order) {
        return order == null ? 0 : order;
    }

    private Track requireTrack(UUID id) {
        return trackRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("Track not found"));
    }

    private Course requireCourse(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("Course not found"));
    }

    private Module requireModule(UUID id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("Module not found"));
    }

    private Lesson requireLesson(UUID id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("Lesson not found"));
    }
}
