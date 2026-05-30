package com.itshaharcha.assessment.service;

import com.itshaharcha.assessment.dto.request.SaveAnswersInput;
import com.itshaharcha.assessment.dto.request.SubmitInput;
import com.itshaharcha.assessment.dto.response.ExamResultResponse;
import com.itshaharcha.assessment.dto.response.ExamSessionResponse;
import com.itshaharcha.assessment.dto.response.SessionQuestionResponse;

import java.util.List;
import java.util.UUID;

public interface SessionService {

    ExamSessionResponse start(UUID examId);

    ExamSessionResponse getSession(UUID sessionId);

    List<SessionQuestionResponse> getSessionQuestions(UUID sessionId);

    void saveAnswers(UUID sessionId, SaveAnswersInput input);

    ExamResultResponse submit(UUID sessionId, SubmitInput input);
}
