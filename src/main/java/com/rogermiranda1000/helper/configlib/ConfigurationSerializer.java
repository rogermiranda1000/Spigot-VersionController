package com.rogermiranda1000.helper.configlib;

import com.rogermiranda1000.helper.configlib.ConfigurationElements.FieldElement;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class ConfigurationSerializer<T> extends TypeSerializer<T, FieldElement> {
    ConfigurationSerializer(Class<T> configurationType, ConfigurationProperties properties) {
        super(Validator.requireConfigurationClass(configurationType), properties);
    }

    @Override
    public T deserialize(Map<?, ?> serializedConfiguration) {
        final T result = Reflect.callNoParamConstructor(type);

        for (final FieldElement element : elements()) {
            final String formattedName = formatter.format(element.name());

            if (!serializedConfiguration.containsKey(formattedName))
                continue;

            final Object serializedValue = serializedConfiguration.get(formattedName);
            final Field field = element.element();

            if ((serializedValue == null) && properties.inputNulls()) {
                requireNonPrimitiveFieldType(field);
                Reflect.setValue(field, result, null);
            } else if (serializedValue != null) {
                Object deserializeValue = deserialize(element, serializedValue);
                Reflect.setValue(field, result, deserializeValue);
            }
        }

        return result;
    }

    @Override
    protected void requireSerializableElements() {
        if (serializers.isEmpty()) {
            String msg = "Configuration class '" + type.getSimpleName() + "' " +
                         "does not contain any (de-)serializable fields.";
            throw new ConfigurationException(msg);
        }
    }

    @Override
    protected String baseDeserializeExceptionMessage(FieldElement element, Object value) {
        return String.format("Deserialization of value '%s' with type '%s' for field '%s' failed.",
                        value, value.getClass(), element.element());
    }

    @Override
    protected List<FieldElement> elements() {
        return FieldExtractors.CONFIGURATION.extract(type)
                .filter(properties.getFieldFilter())
                .map(FieldElement::new)
                .collect(Collectors.toList());
    }

    @Override
    T newDefaultInstance() {
        return Reflect.callNoParamConstructor(type);
    }

    private static void requireNonPrimitiveFieldType(Field field) {
        if (field.getType().isPrimitive()) {
            String msg = String.format("Cannot set field '%s' to null value. Primitive types cannot be assigned null.",
                    field);
            throw new ConfigurationException(msg);
        }
    }

    Class<T> getConfigurationType() {
        return type;
    }
}
