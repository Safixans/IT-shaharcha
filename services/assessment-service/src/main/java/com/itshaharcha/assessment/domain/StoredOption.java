package com.itshaharcha.assessment.domain;

/**
 * A stored answer-key option. For choice types it's a selectable option (id+text) with a
 * correctness flag; for INPUT each acceptable answer is stored as a correct option.
 */
public record StoredOption(String id, String text, boolean correct) {
}
