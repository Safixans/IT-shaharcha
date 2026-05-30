package com.itshaharcha.analytics.scoring;

import java.util.Map;
import java.util.Set;

/**
 * The agreed event catalog and the rules for folding an event into progress:
 * recognized {@code type}s, the points each awards, the counter key it bumps, and the
 * milestone it may unlock. Centralized so ingestion stays a thin, deterministic mapping.
 */
public final class EventScoring {

    /** Closed set of recognized event types (shared/events.yaml EventType). */
    public static final Set<String> RECOGNIZED_TYPES = Set.of(
            "identity.account.registered",
            "identity.account.verified",
            "identity.account.suspended",
            "identity.login.succeeded",
            "identity.login.failed",
            "identity.profile.updated",
            "learning.course.enrolled",
            "learning.lesson.started",
            "learning.lesson.completed",
            "learning.tutorial.watched",
            "learning.doc.read",
            "learning.typing.session.completed",
            "learning.skill.leveled_up",
            "assessment.exam.started",
            "assessment.exam.submitted",
            "assessment.exam.scored",
            "assessment.mock.completed",
            "portfolio.certificate.uploaded",
            "portfolio.certificate.verified",
            "portfolio.education.added",
            "portfolio.item.added",
            "portfolio.published",
            "analytics.milestone.reached",
            "analytics.ranking.updated");

    private static final Map<String, Integer> POINTS = Map.ofEntries(
            Map.entry("identity.login.succeeded", 1),
            Map.entry("learning.course.enrolled", 10),
            Map.entry("learning.lesson.completed", 50),
            Map.entry("learning.tutorial.watched", 15),
            Map.entry("learning.doc.read", 5),
            Map.entry("learning.typing.session.completed", 20),
            Map.entry("learning.skill.leveled_up", 100),
            Map.entry("assessment.exam.submitted", 10),
            Map.entry("assessment.exam.scored", 100),
            Map.entry("assessment.mock.completed", 50),
            Map.entry("portfolio.certificate.uploaded", 10),
            Map.entry("portfolio.certificate.verified", 75),
            Map.entry("portfolio.education.added", 10),
            Map.entry("portfolio.item.added", 15),
            Map.entry("portfolio.published", 30));

    /** First-time achievements: type -> milestone key (awarded once per account). */
    private static final Map<String, String> MILESTONES = Map.of(
            "learning.lesson.completed", "first_lesson",
            "learning.skill.leveled_up", "skill_leveled_up",
            "assessment.exam.scored", "first_exam",
            "assessment.mock.completed", "first_mock_exam",
            "portfolio.certificate.verified", "verified_certificate",
            "portfolio.published", "portfolio_published");

    /** Points awarded per level tier (cumulative points / this = level). */
    private static final long POINTS_PER_LEVEL = 500;

    private EventScoring() {
    }

    public static boolean isRecognized(String type) {
        return type != null && RECOGNIZED_TYPES.contains(type);
    }

    public static int pointsFor(String type) {
        return POINTS.getOrDefault(type, 0);
    }

    /** Counter key bumped for this event: the type with its {@code <source>.} prefix stripped. */
    public static String counterKey(String type, String source) {
        if (type == null) {
            return "unknown";
        }
        String prefix = source + ".";
        return type.startsWith(prefix) ? type.substring(prefix.length()) : type;
    }

    /** The milestone this event unlocks (first time only), or {@code null}. */
    public static String milestoneFor(String type) {
        return MILESTONES.get(type);
    }

    public static int levelFor(long points) {
        return (int) (points / POINTS_PER_LEVEL);
    }
}
