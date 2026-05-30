package com.itshaharcha.assessment.convert;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists an arbitrary JSON answer value (a choice id, an array of ids, a boolean,
 * or free text) as a JSON text column. Deserializes back to the natural Java type
 * (String, Boolean, Number, List, Map).
 */
@Converter
public class JsonValueConverter implements AttributeConverter<Object, String> {

    @Override
    public String convertToDatabaseColumn(Object attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return JsonMapperHolder.MAPPER.writeValueAsString(attribute);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to serialize answer value", ex);
        }
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return JsonMapperHolder.MAPPER.readValue(dbData, Object.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to deserialize answer value", ex);
        }
    }
}
