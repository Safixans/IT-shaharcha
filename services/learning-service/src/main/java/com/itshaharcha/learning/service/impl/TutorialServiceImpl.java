package com.itshaharcha.learning.service.impl;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.learning.dto.request.TutorialInput;
import com.itshaharcha.learning.dto.request.TutorialWatchedInput;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.dto.response.TutorialResponse;
import com.itshaharcha.learning.entity.Tutorial;
import com.itshaharcha.learning.entity.TutorialWatch;
import com.itshaharcha.learning.event.LearningEventPublisher;
import com.itshaharcha.learning.mapper.LearningMapper;
import com.itshaharcha.learning.repository.TutorialRepository;
import com.itshaharcha.learning.repository.TutorialWatchRepository;
import com.itshaharcha.learning.security.SecurityUtils;
import com.itshaharcha.learning.service.TutorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TutorialServiceImpl implements TutorialService {

    private final TutorialRepository tutorialRepository;
    private final TutorialWatchRepository tutorialWatchRepository;
    private final LearningMapper mapper;
    private final LearningEventPublisher events;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TutorialResponse> listTutorials(String topic, Pageable pageable) {
        return PageResponse.from(tutorialRepository.search(topic, pageable), mapper::toTutorialResponse);
    }

    @Override
    @Transactional
    public void recordWatched(UUID tutorialId, TutorialWatchedInput input) {
        if (!tutorialRepository.existsById(tutorialId)) {
            throw ApplicationException.notFound("Tutorial not found");
        }
        UUID accountId = SecurityUtils.currentAccountId();
        TutorialWatch watch = tutorialWatchRepository.findByTutorialIdAndAccountId(tutorialId, accountId)
                .orElseGet(() -> {
                    TutorialWatch w = new TutorialWatch();
                    w.setTutorialId(tutorialId);
                    w.setAccountId(accountId);
                    return w;
                });
        watch.setWatchedSeconds(input.watchedSeconds());
        watch.setPositionSeconds(input.positionSeconds());
        watch.setCompleted(Boolean.TRUE.equals(input.completed()));
        tutorialWatchRepository.save(watch);
        events.emit("learning.tutorial.watched", "tutorial", tutorialId, Map.of(
                "watchedSeconds", input.watchedSeconds(),
                "completed", watch.isCompleted()));
    }

    @Override
    @Transactional
    public TutorialResponse createTutorial(TutorialInput input) {
        Tutorial tutorial = new Tutorial();
        apply(tutorial, input);
        return mapper.toTutorialResponse(tutorialRepository.save(tutorial));
    }

    @Override
    @Transactional
    public TutorialResponse updateTutorial(UUID tutorialId, TutorialInput input) {
        Tutorial tutorial = require(tutorialId);
        apply(tutorial, input);
        return mapper.toTutorialResponse(tutorialRepository.save(tutorial));
    }

    @Override
    @Transactional
    public void deleteTutorial(UUID tutorialId) {
        tutorialRepository.delete(require(tutorialId));
    }

    private void apply(Tutorial tutorial, TutorialInput input) {
        tutorial.setTitle(input.title());
        tutorial.setTopic(input.topic());
        tutorial.setVideoUrl(input.videoUrl());
        tutorial.setDurationSeconds(input.durationSeconds());
        tutorial.setThumbnailUrl(input.thumbnailUrl());
        tutorial.setSourceId(input.sourceId());
    }

    private Tutorial require(UUID id) {
        return tutorialRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("Tutorial not found"));
    }
}
