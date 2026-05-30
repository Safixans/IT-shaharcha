package com.itshaharcha.assessment.convert;

import tools.jackson.core.type.TypeReference;
import com.itshaharcha.assessment.dto.SectionScore;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

/** Persists an exam result's per-section scores as a JSON text column. */
@Converter
public class SectionScoreListConverter implements AttributeConverter<List<SectionScore>, String> {

    private static final TypeReference<List<SectionScore>> TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(List<SectionScore> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return JsonMapperHolder.MAPPER.writeValueAsString(attribute);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to serialize section scores", ex);
        }
    }

    @Override
    public List<SectionScore> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }
        try {
            return JsonMapperHolder.MAPPER.readValue(dbData, TYPE);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to deserialize section scores", ex);
        }
    }
}
