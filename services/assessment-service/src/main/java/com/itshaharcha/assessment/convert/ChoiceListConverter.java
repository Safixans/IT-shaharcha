package com.itshaharcha.assessment.convert;

import tools.jackson.core.type.TypeReference;
import com.itshaharcha.assessment.dto.Choice;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

/** Persists a question's choice list as a JSON text column. */
@Converter
public class ChoiceListConverter implements AttributeConverter<List<Choice>, String> {

    private static final TypeReference<List<Choice>> TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(List<Choice> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return JsonMapperHolder.MAPPER.writeValueAsString(attribute);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to serialize choices", ex);
        }
    }

    @Override
    public List<Choice> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }
        try {
            return JsonMapperHolder.MAPPER.readValue(dbData, TYPE);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to deserialize choices", ex);
        }
    }
}
