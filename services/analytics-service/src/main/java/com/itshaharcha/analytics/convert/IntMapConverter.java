package com.itshaharcha.analytics.convert;

import tools.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.LinkedHashMap;
import java.util.Map;

/** Persists a per-domain counter map (metric name -> count) as a JSON text column. */
@Converter
public class IntMapConverter implements AttributeConverter<Map<String, Integer>, String> {

    private static final TypeReference<Map<String, Integer>> TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(Map<String, Integer> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "{}";
        }
        try {
            return JsonMapperHolder.MAPPER.writeValueAsString(attribute);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to serialize counters", ex);
        }
    }

    @Override
    public Map<String, Integer> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return JsonMapperHolder.MAPPER.readValue(dbData, TYPE);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to deserialize counters", ex);
        }
    }
}
