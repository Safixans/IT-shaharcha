package com.itshaharcha.analytics.scoring;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventScoringTest {

    @Test
    void recognizesCatalogTypesAndRejectsUnknown() {
        assertThat(EventScoring.isRecognized("learning.lesson.completed")).isTrue();
        assertThat(EventScoring.isRecognized("assessment.exam.scored")).isTrue();
        assertThat(EventScoring.isRecognized("totally.made.up")).isFalse();
        assertThat(EventScoring.isRecognized(null)).isFalse();
    }

    @Test
    void awardsPointsPerType() {
        assertThat(EventScoring.pointsFor("assessment.exam.scored")).isEqualTo(100);
        assertThat(EventScoring.pointsFor("learning.lesson.completed")).isEqualTo(50);
        assertThat(EventScoring.pointsFor("identity.profile.updated")).isZero();
    }

    @Test
    void counterKeyStripsSourcePrefix() {
        assertThat(EventScoring.counterKey("learning.lesson.completed", "learning"))
                .isEqualTo("lesson.completed");
        assertThat(EventScoring.counterKey("portfolio.published", "portfolio"))
                .isEqualTo("published");
    }

    @Test
    void milestoneDerivedForAchievementsOnly() {
        assertThat(EventScoring.milestoneFor("assessment.mock.completed")).isEqualTo("first_mock_exam");
        assertThat(EventScoring.milestoneFor("portfolio.published")).isEqualTo("portfolio_published");
        assertThat(EventScoring.milestoneFor("identity.login.succeeded")).isNull();
    }

    @Test
    void levelScalesWithPoints() {
        assertThat(EventScoring.levelFor(0)).isZero();
        assertThat(EventScoring.levelFor(499)).isZero();
        assertThat(EventScoring.levelFor(500)).isEqualTo(1);
        assertThat(EventScoring.levelFor(1250)).isEqualTo(2);
    }
}
