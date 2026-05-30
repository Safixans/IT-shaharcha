package com.itshaharcha.portfolio.service;

import com.itshaharcha.portfolio.dto.request.EducationCreate;
import com.itshaharcha.portfolio.dto.response.EducationResponse;

import java.util.List;
import java.util.UUID;

public interface EducationService {

    List<EducationResponse> list();

    EducationResponse add(EducationCreate input);

    void delete(UUID educationId);
}
