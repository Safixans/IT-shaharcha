package com.itshaharcha.analytics.service;

import com.itshaharcha.analytics.dto.response.Milestone;
import com.itshaharcha.analytics.dto.response.PageResponse;
import com.itshaharcha.analytics.dto.response.ProgressOverview;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface ProgressService {

    ProgressOverview progress(UUID accountId, Instant from, Instant to);

    PageResponse<Milestone> milestones(UUID accountId, Pageable pageable);
}
