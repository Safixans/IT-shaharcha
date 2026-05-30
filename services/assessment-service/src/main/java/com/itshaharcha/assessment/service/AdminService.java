package com.itshaharcha.assessment.service;

import com.itshaharcha.assessment.dto.request.ExamInput;
import com.itshaharcha.assessment.dto.request.QuestionInput;
import com.itshaharcha.assessment.dto.request.SectionInput;
import com.itshaharcha.assessment.dto.response.ExamResponse;
import com.itshaharcha.assessment.dto.response.QuestionResponse;
import com.itshaharcha.assessment.dto.response.SectionResponse;

import java.util.UUID;

public interface AdminService {

    ExamResponse createExam(ExamInput input);

    ExamResponse updateExam(UUID examId, ExamInput input);

    void deleteExam(UUID examId);

    SectionResponse createSection(UUID examId, SectionInput input);

    SectionResponse updateSection(UUID sectionId, SectionInput input);

    void deleteSection(UUID sectionId);

    QuestionResponse createQuestion(UUID sectionId, QuestionInput input);

    QuestionResponse updateQuestion(UUID questionId, QuestionInput input);

    void deleteQuestion(UUID questionId);
}
