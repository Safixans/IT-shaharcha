package com.itshaharcha.analytics.service;

import com.itshaharcha.analytics.dto.response.Dashboard;

import java.time.Instant;
import java.util.UUID;

public interface DashboardService {

    Dashboard dashboard(UUID accountId, String granularity, Instant from, Instant to);
}
