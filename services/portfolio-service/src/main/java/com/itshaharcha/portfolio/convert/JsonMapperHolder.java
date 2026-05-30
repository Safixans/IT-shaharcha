package com.itshaharcha.portfolio.convert;

import tools.jackson.databind.json.JsonMapper;

/**
 * JPA {@link jakarta.persistence.AttributeConverter}s are instantiated by Hibernate
 * outside the Spring context, so they cannot have a managed {@code JsonMapper}
 * injected. They share this single configured instance instead.
 */
final class JsonMapperHolder {

    static final JsonMapper MAPPER = JsonMapper.builder().build();

    private JsonMapperHolder() {
    }
}
