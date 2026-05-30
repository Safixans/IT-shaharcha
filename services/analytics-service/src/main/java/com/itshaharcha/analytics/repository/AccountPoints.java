package com.itshaharcha.analytics.repository;

import java.util.UUID;

/** Projection: an account's summed points for an overall (cross-domain) leaderboard. */
public interface AccountPoints {

    UUID getAccountId();

    long getPoints();
}
