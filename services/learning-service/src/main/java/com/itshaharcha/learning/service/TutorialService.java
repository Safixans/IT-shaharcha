package com.itshaharcha.learning.service;

import com.itshaharcha.learning.dto.request.TutorialInput;
import com.itshaharcha.learning.dto.request.TutorialWatchedInput;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.dto.response.TutorialResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TutorialService {

    PageResponse<TutorialResponse> listTutorials(String topic, Pageable pageable);

    void recordWatched(UUID tutorialId, TutorialWatchedInput input);

    TutorialResponse createTutorial(TutorialInput input);

    TutorialResponse updateTutorial(UUID tutorialId, TutorialInput input);

    void deleteTutorial(UUID tutorialId);
}
