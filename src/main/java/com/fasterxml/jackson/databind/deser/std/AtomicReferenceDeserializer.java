package com.fasterxml.jackson.databind.deser.std;

import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

public class AtomicReferenceDeserializer
    extends ReferenceTypeDeserializer<AtomicReference<Object>>
{
    private static final @Initialized long serialVersionUID = 1L;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    /**
     * @since 2.9
     */
    public AtomicReferenceDeserializer(@Initialized JavaType fullType, @Initialized @Nullable ValueInstantiator inst,
            @Initialized
            TypeDeserializer typeDeser, @Initialized JsonDeserializer<?> deser)
    {
        super(fullType, inst, typeDeser, deser);
    }

    /*
    /**********************************************************
    /* Abstract method implementations
    /**********************************************************
     */

    @Override
    public AtomicReferenceDeserializer withResolved(@Initialized AtomicReferenceDeserializer this, @Initialized TypeDeserializer typeDeser, @Initialized JsonDeserializer<?> valueDeser) {
        return new AtomicReferenceDeserializer(_fullType, _valueInstantiator,
                typeDeser, valueDeser);
    }
    @Override
    public AtomicReference<Object> getNullValue(@Initialized AtomicReferenceDeserializer this, @Initialized DeserializationContext ctxt) {
        return new AtomicReference<Object>();
    }

    @Override
    public Object getEmptyValue(@Initialized AtomicReferenceDeserializer this, @Initialized DeserializationContext ctxt) {
        return new AtomicReference<Object>();
    }
    
    @Override
    public AtomicReference<Object> referenceValue(@Initialized AtomicReferenceDeserializer this, @Initialized Object contents) {
        return new AtomicReference<Object>(contents);
    }

    @Override
    public Object getReferenced(@Initialized AtomicReferenceDeserializer this, @Initialized AtomicReference<Object> reference) {
        return reference.get();
    }

    @Override // since 2.9
    public AtomicReference<Object> updateReference(@Initialized AtomicReferenceDeserializer this, @Initialized AtomicReference<Object> reference, @Initialized Object contents) {
        reference.set(contents);
        return reference;
    }

    @Override // since 2.9
    public Boolean supportsUpdate(@Initialized AtomicReferenceDeserializer this, @Initialized DeserializationConfig config) {
        // yes; regardless of value deserializer reference itself may be updated
        return Boolean.TRUE;
    }
}
