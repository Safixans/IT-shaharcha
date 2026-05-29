package com.itshaharcha.learning.service;

import com.itshaharcha.learning.dto.request.LessonCompletedInput;
import com.itshaharcha.learning.dto.response.EnrollmentResponse;
import com.itshaharcha.learning.dto.response.LessonProgressResponse;
import com.itshaharcha.learning.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EnrollmentService {

    EnrollmentResponse enroll(UUID courseId);

    PageResponse<EnrollmentResponse> listMyEnrollments(Pageable pageable);

    void startLesson(UUID lessonId);

    LessonProgressResponse completeLesson(UUID lessonId, LessonCompletedInput input);
}
