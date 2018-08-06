package com.fasterxml.jackson.databind.deser.impl;

import org.checkerframework.checker.initialization.qual.Initialized;
import java.io.IOException;
import java.lang.annotation.Annotation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;

/**
 * Specialized {@link SettableBeanProperty} implementation used
 * for virtual property that represents Object Id that is used
 * for some POJO types (or properties).
 */
public final class ObjectIdValueProperty
    extends SettableBeanProperty
{
    private static final @Initialized long serialVersionUID = 1L;

    protected final @Initialized ObjectIdReader _objectIdReader;

    public ObjectIdValueProperty(@Initialized ObjectIdReader objectIdReader,
            @Initialized
            PropertyMetadata metadata)
    {
        super(objectIdReader.propertyName, objectIdReader.getIdType(), metadata,
                objectIdReader.getDeserializer());
        _objectIdReader = objectIdReader;
    }

    protected ObjectIdValueProperty(@Initialized ObjectIdValueProperty src, @Initialized JsonDeserializer<?> deser,
            @Initialized
            NullValueProvider nva)
    {
        super(src, deser, nva);
        _objectIdReader = src._objectIdReader;
    }

    protected ObjectIdValueProperty(@Initialized ObjectIdValueProperty src, @Initialized PropertyName newName) {
        super(src, newName);
        _objectIdReader = src._objectIdReader;
    }

    @Override
    public SettableBeanProperty withName(@Initialized ObjectIdValueProperty this, @Initialized PropertyName newName) {
        return new ObjectIdValueProperty(this, newName);
    }

    @Override
    public SettableBeanProperty withValueDeserializer(@Initialized ObjectIdValueProperty this, @Initialized JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        return new ObjectIdValueProperty(this, deser, _nullProvider);
    }

    @Override
    public SettableBeanProperty withNullProvider(@Initialized ObjectIdValueProperty this, @Initialized NullValueProvider nva) {
        return new ObjectIdValueProperty(this, _valueDeserializer, nva);
    }

    // // // BeanProperty impl
    
    @Override
    public <A extends Annotation> A getAnnotation(@Initialized ObjectIdValueProperty this, @Initialized Class<A> acls) {
        return null;
    }

    @Override public AnnotatedMember getMember(@Initialized ObjectIdValueProperty this) {  return null; }

    /*
    /**********************************************************
    /* Deserialization methods
    /**********************************************************
     */

    @Override
    public void deserializeAndSet(@Initialized ObjectIdValueProperty this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt,
            @Initialized
            Object instance) throws IOException
    {
        deserializeSetAndReturn(p, ctxt, instance);
    }

    @Override
    public Object deserializeSetAndReturn(@Initialized ObjectIdValueProperty this, @Initialized JsonParser p,
    		@Initialized
    		DeserializationContext ctxt, @Initialized Object instance) throws IOException
    {
        /* 02-Apr-2015, tatu: Actually, as per [databind#742], let it be;
         *  missing or null id is needed for some cases, such as cases where id
         *  will be generated externally, at a later point, and is not available
         *  quite yet. Typical use case is with DB inserts.
         */
        // note: no null checks (unlike usually); deserializer should fail if one found
        if (p.hasToken(JsonToken.VALUE_NULL)) {
            return null;
        }
        Object id = _valueDeserializer.deserialize(p, ctxt);
        ReadableObjectId roid = ctxt.findObjectId(id, _objectIdReader.generator, _objectIdReader.resolver);
        roid.bindItem(instance);
        // also: may need to set a property value as well
        SettableBeanProperty idProp = _objectIdReader.idProperty;
        if (idProp != null) {
            return idProp.setAndReturn(instance, id);
        }
        return instance;
    }

    @Override
    public void set(@Initialized ObjectIdValueProperty this, @Initialized Object instance, @Initialized Object value) throws IOException {
        setAndReturn(instance, value);
    }

    @Override
    public Object setAndReturn(@Initialized ObjectIdValueProperty this, @Initialized Object instance, @Initialized Object value) throws IOException
    {
        SettableBeanProperty idProp = _objectIdReader.idProperty;
        if (idProp == null) {
            throw new UnsupportedOperationException(
                    "Should not call set() on ObjectIdProperty that has no SettableBeanProperty");
        }
        return idProp.setAndReturn(instance, value);
    }
}
