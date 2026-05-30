package com.itshaharcha.analytics.service;

import com.itshaharcha.analytics.dto.response.Leaderboard;
import com.itshaharcha.analytics.dto.response.RankEntry;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface RankingService {

    Leaderboard leaderboard(String domain, String period, Pageable pageable);

    List<RankEntry> myRank(UUID accountId, String period);
}
