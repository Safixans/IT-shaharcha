package com.itshaharcha.learning.service;

import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.dto.response.RoadmapCardResponse;
import com.itshaharcha.learning.dto.response.RoadmapDetailResponse;
import org.springframework.data.domain.Pageable;

public interface RoadmapService {

    PageResponse<RoadmapCardResponse> listRoadmaps(String q, String kind, Pageable pageable);

    RoadmapDetailResponse getRoadmap(String slug);
}
