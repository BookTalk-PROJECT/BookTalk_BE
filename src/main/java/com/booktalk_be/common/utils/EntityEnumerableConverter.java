package com.booktalk_be.common.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Objects;


@Converter
public abstract class EntityEnumerableConverter<T extends EntityEnumerable> implements AttributeConverter<T, String> {

    private final Class<T> clazz;

    public EntityEnumerableConverter(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        if (Objects.isNull(attribute)) {
            return null;
        }
        return attribute.getType();
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (dbData.isBlank()) {
            return null;
        }
        T[] enumConstants = clazz.getEnumConstants();
        for (T constant : enumConstants) {
            if (constant.getType().equals(dbData)) {
                return constant;
            }
        }
        throw new UnsupportedOperationException("지원하지 않는 enum 형식입니다.");
    }
}