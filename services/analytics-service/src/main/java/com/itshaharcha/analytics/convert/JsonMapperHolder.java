package com.itshaharcha.analytics.convert;

import tools.jackson.databind.json.JsonMapper;

/**
 * JPA {@link jakarta.persistence.AttributeConverter}s are instantiated by Hibernate
 * outside the Spring context, so they share this single configured instance.
 */
public final class JsonMapperHolder {

    public static final JsonMapper MAPPER = JsonMapper.builder().build();

    private JsonMapperHolder() {
    }
}
