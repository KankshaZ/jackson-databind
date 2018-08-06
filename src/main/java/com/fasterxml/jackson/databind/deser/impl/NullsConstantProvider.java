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
public class NullsConstantProvider
    implements NullValueProvider, java.io.Serializable
{
    private static final @Initialized long serialVersionUID = 1L;

    private final static @Initialized NullsConstantProvider SKIPPER = new NullsConstantProvider(null);

    private final static @Initialized NullsConstantProvider NULLER = new NullsConstantProvider(null);
    
    protected final @Initialized Object _nullValue;

    protected final @Initialized AccessPattern _access;

    protected NullsConstantProvider(@Initialized @Nullable Object nvl) {
        _nullValue = nvl;
        _access = (_nullValue == null) ? AccessPattern.ALWAYS_NULL
                : AccessPattern.CONSTANT;
    }

    /**
     * Static accessor for a stateless instance used as marker, to indicate
     * that all input `null` values should be skipped (ignored), so that
     * no corresponding property value is set (with POJOs), and no content
     * values (array/Collection elements, Map entries) are added.
     */
    public static NullsConstantProvider skipper() {
        return SKIPPER;
    }

    public static NullsConstantProvider nuller() {
        return NULLER;
    }

    public static NullsConstantProvider forValue(@Initialized Object nvl) {
        if (nvl == null) {
            return NULLER;
        }
        return new NullsConstantProvider(nvl);
    }

    /**
     * Utility method that can be used to check if given null value provider
     * is "skipper", marker provider that means that all input `null`s should
     * be skipped (ignored), instead of converted
     */
    public static boolean isSkipper(@Initialized NullValueProvider p) {
        return (p == SKIPPER);
    }

    /**
     * Utility method that can be used to check if given null value provider
     * is "nuller", no-operation provider that will always simply return
     * Java `null` for any and all input `null`s.
     */
    public static boolean isNuller(NullValueProvider p) {
        return (p == NULLER);
    }
    
    @Override
    public AccessPattern getNullAccessPattern(@Initialized NullsConstantProvider this) {
        return _access;
    }
    
    @Override
    public Object getNullValue(@Initialized NullsConstantProvider this, @Initialized DeserializationContext ctxt) {
        return _nullValue;
    }
}
