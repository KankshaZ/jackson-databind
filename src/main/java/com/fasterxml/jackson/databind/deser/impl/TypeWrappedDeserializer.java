package com.fasterxml.jackson.databind.deser.impl;

import org.checkerframework.checker.initialization.qual.Initialized;
import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

/**
 * Simple deserializer that will call configured type deserializer, passing
 * in configured data deserializer, and exposing it all as a simple
 * deserializer.
 * This is necessary when there is no "parent" deserializer which could handle
 * details of calling a {@link TypeDeserializer}, most commonly used with
 * root values.
 */
public final class TypeWrappedDeserializer
    extends JsonDeserializer<Object>
    implements java.io.Serializable // since 2.5
{
    private static final @Initialized long serialVersionUID = 1L;

    final protected @Initialized TypeDeserializer _typeDeserializer;
    final protected @Initialized JsonDeserializer<Object> _deserializer;

    @SuppressWarnings("unchecked")
    public TypeWrappedDeserializer(@Initialized TypeDeserializer typeDeser, @Initialized JsonDeserializer<?> deser)
    {
        super();
        _typeDeserializer = typeDeser;
        _deserializer = (JsonDeserializer<Object>) deser;
    }

    @Override
    public Class<?> handledType(@Initialized TypeWrappedDeserializer this) {
        return _deserializer.handledType();
    }

    @Override // since 2.9
    public Boolean supportsUpdate(@Initialized TypeWrappedDeserializer this, @Initialized DeserializationConfig config) {
        return _deserializer.supportsUpdate(config);
    }
    
    @Override
    public JsonDeserializer<?> getDelegatee(@Initialized TypeWrappedDeserializer this) {
        return _deserializer.getDelegatee();
    }

    @Override
    public Collection<Object> getKnownPropertyNames(@Initialized TypeWrappedDeserializer this) {
        return _deserializer.getKnownPropertyNames();
    }

    @Override
    public Object getNullValue(@Initialized TypeWrappedDeserializer this, @Initialized DeserializationContext ctxt) throws JsonMappingException {
        return _deserializer.getNullValue(ctxt);
    }

    @Override
    public Object getEmptyValue(@Initialized TypeWrappedDeserializer this, @Initialized DeserializationContext ctxt) throws JsonMappingException {
        return _deserializer.getEmptyValue(ctxt);
    }
    
    @Override
    public Object deserialize(@Initialized TypeWrappedDeserializer this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt) throws IOException
    {
        return _deserializer.deserializeWithType(p, ctxt, _typeDeserializer);
    }

    @Override
    public Object deserializeWithType(@Initialized TypeWrappedDeserializer this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt,
        @Initialized
        TypeDeserializer typeDeserializer) throws IOException
    {
        // should never happen? (if it can, could call on that object)
        throw new IllegalStateException("Type-wrapped deserializer's deserializeWithType should never get called");
    }

    @Override
    public Object deserialize(@Initialized TypeWrappedDeserializer this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt,
            @Initialized
            Object intoValue) throws IOException
    {
        /* 01-Mar-2013, tatu: Hmmh. Tough call as to what to do... need
         *   to delegate, but will this work reliably? Let's just hope so:
         */
        return _deserializer.deserialize(p,  ctxt, intoValue);
    }
}
