package com.itshaharcha.learning.service;

import com.itshaharcha.learning.dto.request.ContentSourceInput;
import com.itshaharcha.learning.dto.response.ContentSourceResponse;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.dto.response.SourceSyncRunResponse;
import com.itshaharcha.learning.entity.SourceType;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ContentSourceService {

    PageResponse<ContentSourceResponse> listSources(SourceType type, Pageable pageable);

    ContentSourceResponse getSource(UUID sourceId);

    ContentSourceResponse createSource(ContentSourceInput input);

    ContentSourceResponse updateSource(UUID sourceId, ContentSourceInput input);

    void deleteSource(UUID sourceId);

    SourceSyncRunResponse sync(UUID sourceId);
}
