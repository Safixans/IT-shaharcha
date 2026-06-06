package com.itshaharcha.assessment.dto.request;

/** Toggle a unit active/inactive. Null defaults to activating. */
public record ActivationRequest(Boolean active) {
}
