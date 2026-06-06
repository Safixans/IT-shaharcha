package com.itshaharcha.assessment.service;

import java.util.List;

/** Builds a jsonb array literal for tag containment filtering (the repos use {@code @>}). */
final class TagSupport {

    private TagSupport() {
    }

    /** First non-blank tag as a JSON array literal like {@code ["academic"]}, or null if none. */
    static String firstTagLiteral(List<String> tags) {
        if (tags == null) {
            return null;
        }
        return tags.stream()
                .filter(t -> t != null && !t.isBlank())
                .findFirst()
                .map(t -> "[\"" + t.trim().replace("\"", "\\\"") + "\"]")
                .orElse(null);
    }
}
