package com.itshaharcha.learning.mapper;

import com.itshaharcha.learning.dto.response.ContentSourceResponse;
import com.itshaharcha.learning.dto.response.CourseDetailResponse;
import com.itshaharcha.learning.dto.response.CourseResponse;
import com.itshaharcha.learning.dto.response.DocResponse;
import com.itshaharcha.learning.dto.response.EnrollmentResponse;
import com.itshaharcha.learning.dto.response.LessonProgressResponse;
import com.itshaharcha.learning.dto.response.LessonResponse;
import com.itshaharcha.learning.dto.response.ModuleResponse;
import com.itshaharcha.learning.dto.response.SourceSyncRunResponse;
import com.itshaharcha.learning.dto.response.TrackResponse;
import com.itshaharcha.learning.dto.response.TutorialResponse;
import com.itshaharcha.learning.dto.response.TypingLessonResponse;
import com.itshaharcha.learning.dto.response.TypingSessionResponse;
import com.itshaharcha.learning.entity.ContentSource;
import com.itshaharcha.learning.entity.Course;
import com.itshaharcha.learning.entity.Doc;
import com.itshaharcha.learning.entity.Enrollment;
import com.itshaharcha.learning.entity.Lesson;
import com.itshaharcha.learning.entity.LessonProgress;
import com.itshaharcha.learning.entity.Module;
import com.itshaharcha.learning.entity.SourceSyncRun;
import com.itshaharcha.learning.entity.Track;
import com.itshaharcha.learning.entity.Tutorial;
import com.itshaharcha.learning.entity.TypingLesson;
import com.itshaharcha.learning.entity.TypingSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LearningMapper {

    TrackResponse toTrackResponse(Track track, int courseCount);

    CourseResponse toCourseResponse(Course course, int lessonCount);

    CourseDetailResponse toCourseDetail(Course course, int lessonCount, List<ModuleResponse> modules);

    @Mapping(target = "order", source = "module.orderIndex")
    ModuleResponse toModuleResponse(Module module, List<LessonResponse> lessons);

    @Mapping(target = "order", source = "orderIndex")
    LessonResponse toLessonResponse(Lesson lesson);

    EnrollmentResponse toEnrollmentResponse(Enrollment enrollment);

    LessonProgressResponse toLessonProgressResponse(LessonProgress progress, double courseProgressPercent);

    TutorialResponse toTutorialResponse(Tutorial tutorial);

    DocResponse toDocResponse(Doc doc);

    TypingLessonResponse toTypingLessonResponse(TypingLesson lesson);

    TypingSessionResponse toTypingSessionResponse(TypingSession session);

    ContentSourceResponse toContentSourceResponse(ContentSource source);

    @Mapping(target = "runId", source = "id")
    SourceSyncRunResponse toSyncRunResponse(SourceSyncRun syncRun);
}
