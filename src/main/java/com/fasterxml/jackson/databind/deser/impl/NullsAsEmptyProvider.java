package com.fasterxml.jackson.databind.deser.impl;

import org.checkerframework.checker.initialization.qual.Initialized;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.util.AccessPattern;

/**
 * Simple {@link NullValueProvider} that will always throw a
 * {@link InvalidNullException} when a null is encountered.
 */
public class NullsAsEmptyProvider
    implements NullValueProvider, java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    protected final JsonDeserializer<?> _deserializer;

    public NullsAsEmptyProvider(@Initialized JsonDeserializer<?> deser) {
        _deserializer = deser;
    }

    @Override
    public AccessPattern getNullAccessPattern(@Initialized NullsAsEmptyProvider this) {
        return AccessPattern.DYNAMIC;
    }

    @Override
    public Object getNullValue(@Initialized NullsAsEmptyProvider this, @Initialized DeserializationContext ctxt)
            throws JsonMappingException {
        return _deserializer.getEmptyValue(ctxt);
    }
}
