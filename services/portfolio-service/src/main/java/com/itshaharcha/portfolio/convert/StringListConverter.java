package com.itshaharcha.portfolio.convert;

import tools.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

/** Persists a portfolio item's tag list as a JSON text column. */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final TypeReference<List<String>> TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return JsonMapperHolder.MAPPER.writeValueAsString(attribute);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to serialize tags", ex);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }
        try {
            return JsonMapperHolder.MAPPER.readValue(dbData, TYPE);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to deserialize tags", ex);
        }
    }
}
