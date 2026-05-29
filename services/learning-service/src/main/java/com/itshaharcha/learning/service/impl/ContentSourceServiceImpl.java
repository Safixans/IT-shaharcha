package com.itshaharcha.learning.service.impl;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.learning.dto.request.ContentSourceInput;
import com.itshaharcha.learning.dto.response.ContentSourceResponse;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.dto.response.SourceSyncRunResponse;
import com.itshaharcha.learning.entity.ContentSource;
import com.itshaharcha.learning.entity.SourceStatus;
import com.itshaharcha.learning.entity.SourceSyncRun;
import com.itshaharcha.learning.entity.SourceType;
import com.itshaharcha.learning.entity.SyncStatus;
import com.itshaharcha.learning.mapper.LearningMapper;
import com.itshaharcha.learning.repository.ContentSourceRepository;
import com.itshaharcha.learning.repository.SourceSyncRunRepository;
import com.itshaharcha.learning.service.ContentSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentSourceServiceImpl implements ContentSourceService {

    private final ContentSourceRepository contentSourceRepository;
    private final SourceSyncRunRepository sourceSyncRunRepository;
    private final LearningMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ContentSourceResponse> listSources(SourceType type, Pageable pageable) {
        return PageResponse.from(contentSourceRepository.search(type, pageable),
                mapper::toContentSourceResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ContentSourceResponse getSource(UUID sourceId) {
        return mapper.toContentSourceResponse(require(sourceId));
    }

    @Override
    @Transactional
    public ContentSourceResponse createSource(ContentSourceInput input) {
        ContentSource source = new ContentSource();
        apply(source, input);
        source.setStatus(SourceStatus.active);
        return mapper.toContentSourceResponse(contentSourceRepository.save(source));
    }

    @Override
    @Transactional
    public ContentSourceResponse updateSource(UUID sourceId, ContentSourceInput input) {
        ContentSource source = require(sourceId);
        apply(source, input);
        return mapper.toContentSourceResponse(contentSourceRepository.save(source));
    }

    @Override
    @Transactional
    public void deleteSource(UUID sourceId) {
        contentSourceRepository.delete(require(sourceId));
    }

    @Override
    @Transactional
    public SourceSyncRunResponse sync(UUID sourceId) {
        ContentSource source = require(sourceId);
        SourceSyncRun run = new SourceSyncRun();
        run.setSourceId(source.getId());
        run.setStatus(SyncStatus.queued);
        run.setStartedAt(Instant.now());
        SourceSyncRun saved = sourceSyncRunRepository.save(run);
        return mapper.toSyncRunResponse(saved);
    }

    private void apply(ContentSource source, ContentSourceInput input) {
        source.setName(input.name());
        source.setType(input.type());
        source.setTarget(input.target());
        source.setUrl(input.url());
        source.setEnabled(input.enabled() == null || input.enabled());
        source.setSchedule(input.schedule());
        source.setDefaultTopic(input.defaultTopic());
    }

    private ContentSource require(UUID id) {
        return contentSourceRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("Content source not found"));
    }
}
