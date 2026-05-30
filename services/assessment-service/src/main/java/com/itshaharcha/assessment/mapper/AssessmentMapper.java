package com.itshaharcha.assessment.mapper;

import com.itshaharcha.assessment.dto.response.ExamDetailResponse;
import com.itshaharcha.assessment.dto.response.ExamResponse;
import com.itshaharcha.assessment.dto.response.ExamResultResponse;
import com.itshaharcha.assessment.dto.response.ExamSessionResponse;
import com.itshaharcha.assessment.dto.response.QuestionResponse;
import com.itshaharcha.assessment.dto.response.SectionResponse;
import com.itshaharcha.assessment.dto.response.SessionQuestionResponse;
import com.itshaharcha.assessment.entity.Exam;
import com.itshaharcha.assessment.entity.ExamResult;
import com.itshaharcha.assessment.entity.ExamSession;
import com.itshaharcha.assessment.entity.Question;
import com.itshaharcha.assessment.entity.Section;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssessmentMapper {

    @Mapping(target = "isRealExam", source = "exam.realExam")
    ExamResponse toExamResponse(Exam exam, int sectionCount);

    @Mapping(target = "isRealExam", source = "exam.realExam")
    ExamDetailResponse toExamDetail(Exam exam, int sectionCount, List<SectionResponse> sections);

    @Mapping(target = "order", source = "section.orderIndex")
    SectionResponse toSectionResponse(Section section, int questionCount);

    @Mapping(target = "order", source = "orderIndex")
    QuestionResponse toQuestionResponse(Question question);

    @Mapping(target = "order", source = "orderIndex")
    SessionQuestionResponse toSessionQuestion(Question question);

    ExamSessionResponse toSessionResponse(ExamSession session);

    ExamResultResponse toResultResponse(ExamResult result);
}
