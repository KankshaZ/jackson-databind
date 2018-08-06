package com.fasterxml.jackson.databind.deser.impl;

import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.io.IOException;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdResolver;

import com.fasterxml.jackson.core.JsonParser;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;

/**
 * Object that knows how to deserialize Object Ids.
 */
public class ObjectIdReader
    implements java.io.Serializable
{
    private static final @Initialized long serialVersionUID = 1L;

    protected final @Initialized JavaType _idType;

    public final @Initialized PropertyName propertyName;
    
    /**
     * Blueprint generator instance: actual instance will be
     * fetched from {@link SerializerProvider} using this as
     * the key.
     */
    public final @Initialized ObjectIdGenerator<?> generator;

    public final @Initialized ObjectIdResolver resolver;

    /**
     * Deserializer used for deserializing id values.
     */
    protected final @Initialized JsonDeserializer<Object> _deserializer;

    public final @Initialized SettableBeanProperty idProperty;
    
    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */
    
    @SuppressWarnings("unchecked")
    protected ObjectIdReader(@Initialized JavaType t, @Initialized PropertyName propName, @Initialized ObjectIdGenerator<?> gen,
            @Initialized
            JsonDeserializer<?> deser, @Initialized SettableBeanProperty idProp, @Initialized ObjectIdResolver resolver)
    {
        _idType = t;
        propertyName = propName;
        generator = gen;
        this.resolver = resolver;
        _deserializer = (JsonDeserializer<Object>) deser;
        idProperty = idProp;
    }

    /**
     * Factory method called by {@link com.fasterxml.jackson.databind.ser.std.BeanSerializerBase}
     * with the initial information based on standard settings for the type
     * for which serializer is being built.
     */
    public static ObjectIdReader construct(@Initialized JavaType idType, @Initialized PropertyName propName,
            @Initialized
            ObjectIdGenerator<?> generator, @Initialized JsonDeserializer<?> deser,
            @Initialized @Nullable SettableBeanProperty idProp, @Initialized ObjectIdResolver resolver)
    {
        return new ObjectIdReader(idType, propName, generator, deser, idProp, resolver);
    }

    /*
    /**********************************************************
    /* API
    /**********************************************************
     */

    public JsonDeserializer<Object> getDeserializer() {
        return _deserializer;
    }

    public JavaType getIdType() {
        return _idType;
    }

    /**
     * Convenience method, equivalent to calling:
     *<code>
     *  readerInstance.generator.maySerializeAsObject();
     *</code>
     * and used to determine whether Object Ids handled by the underlying
     * generator may be in form of (JSON) Objects.
     * Used for optimizing handling in cases where method returns false.
     * 
     * @since 2.5
     */
    public boolean maySerializeAsObject() {
        return generator.maySerializeAsObject();
    }

    /**
     * Convenience method, equivalent to calling:
     *<code>
     *  readerInstance.generator.isValidReferencePropertyName(name, parser);
     *</code>
     * and used to determine whether Object Ids handled by the underlying
     * generator may be in form of (JSON) Objects.
     * Used for optimizing handling in cases where method returns false.
     * 
     * @since 2.5
     */
    public boolean isValidReferencePropertyName(@Initialized String name, @Initialized JsonParser parser) {
        return generator.isValidReferencePropertyName(name, parser);
    }
    
    /**
     * Method called to read value that is expected to be an Object Reference
     * (that is, value of an Object Id used to refer to another object).
     * 
     * @since 2.3
     */
    public Object readObjectReference(@Initialized JsonParser jp, @Initialized DeserializationContext ctxt) throws IOException {
        return _deserializer.deserialize(jp, ctxt);
    }
}
