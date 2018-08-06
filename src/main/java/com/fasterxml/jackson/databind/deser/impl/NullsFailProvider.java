package com.fasterxml.jackson.databind.deser.impl;

import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.Nullable;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.util.AccessPattern;

/**
 * Simple {@link NullValueProvider} that will always throw a
 * {@link InvalidNullException} when a null is encountered.
 */
public class NullsFailProvider
    implements NullValueProvider, java.io.Serializable
{
    private static final @Initialized long serialVersionUID = 1L;

    protected final @Initialized PropertyName _name;
    protected final @Initialized JavaType _type;

    protected NullsFailProvider(@Initialized @Nullable PropertyName name, @Initialized JavaType type) {
        _name = name;
        _type = type;
    }

    public static NullsFailProvider constructForProperty(@Initialized BeanProperty prop) {
        return new NullsFailProvider(prop.getFullName(), prop.getType());
    }

    public static NullsFailProvider constructForRootValue(@Initialized JavaType t) {
        return new NullsFailProvider(null, t);
    }

    @Override
    public AccessPattern getNullAccessPattern(@Initialized NullsFailProvider this) {
        // Must be called every time to effect the exception...
        return AccessPattern.DYNAMIC;
    }

    @Override
    public Object getNullValue(@Initialized NullsFailProvider this, @Initialized DeserializationContext ctxt)
            throws JsonMappingException {
        throw InvalidNullException.from(ctxt, _name, _type);
    }
}
