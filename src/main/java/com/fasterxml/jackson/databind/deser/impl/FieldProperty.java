package com.fasterxml.jackson.databind.deser.impl;

import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.ClassUtil;

/**
 * This concrete sub-class implements property that is set
 * directly assigning to a Field.
 */
public final class FieldProperty
    extends SettableBeanProperty
{
    private static final @Initialized long serialVersionUID = 1L;

    final protected @Initialized AnnotatedField _annotated;

    /**
     * Actual field to set when deserializing this property.
     * Transient since there is no need to persist; only needed during
     * construction of objects.
     */
    final protected transient @Initialized Field _field;

    /**
     * @since 2.9
     */
    final protected @Initialized boolean _skipNulls;

    public FieldProperty(@Initialized BeanPropertyDefinition propDef, @Initialized JavaType type,
            @Initialized
            @Nullable
            TypeDeserializer typeDeser, @Initialized Annotations contextAnnotations, @Initialized AnnotatedField field)
    {
        super(propDef, type, typeDeser, contextAnnotations);
        _annotated = field;
        _field = field.getAnnotated();
        _skipNulls = NullsConstantProvider.isSkipper(_nullProvider);
    }

    protected FieldProperty(@Initialized FieldProperty src, @Initialized JsonDeserializer<?> deser,
            @Initialized
            NullValueProvider nva) {
        super(src, deser, nva);
        _annotated = src._annotated;
        _field = src._field;
        _skipNulls = NullsConstantProvider.isSkipper(nva);
    }

    protected FieldProperty(@Initialized FieldProperty src, @Initialized PropertyName newName) {
        super(src, newName);
        _annotated = src._annotated;
        _field = src._field;
        _skipNulls = src._skipNulls;
    }

    /**
     * Constructor used for JDK Serialization when reading persisted object
     */
    protected FieldProperty(@Initialized FieldProperty src)
    {
        super(src);
        _annotated = src._annotated;
        Field f = _annotated.getAnnotated();
        if (f == null) {
            throw new IllegalArgumentException("Missing field (broken JDK (de)serialization?)");
        }
        _field = f;
        _skipNulls = src._skipNulls;
    }

    @Override
    public SettableBeanProperty withName(@Initialized FieldProperty this, @Initialized PropertyName newName) {
        return new FieldProperty(this, newName);
    }

    @Override
    public SettableBeanProperty withValueDeserializer(@Initialized FieldProperty this, @Initialized JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        return new FieldProperty(this, deser, _nullProvider);
    }

    @Override
    public SettableBeanProperty withNullProvider(@Initialized FieldProperty this, @Initialized NullValueProvider nva) {
        return new FieldProperty(this, _valueDeserializer, nva);
    }

    @Override
    public void fixAccess(@Initialized FieldProperty this, @Initialized DeserializationConfig config) {
        ClassUtil.checkAndFixAccess(_field,
                config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
    }

    /*
    /**********************************************************
    /* BeanProperty impl
    /**********************************************************
     */
    
    @Override
    public <A extends Annotation> A getAnnotation(@Initialized FieldProperty this, @Initialized Class<A> acls) {
        return (_annotated == null) ? null : _annotated.getAnnotation(acls);
    }

    @Override public AnnotatedMember getMember(@Initialized FieldProperty this) {  return _annotated; }

    /*
    /**********************************************************
    /* Overridden methods
    /**********************************************************
     */

    @Override
    public void deserializeAndSet(@Initialized FieldProperty this, @Initialized JsonParser p,
    		@Initialized
    		DeserializationContext ctxt, @Initialized Object instance) throws IOException
    {
        Object value;
        if (p.hasToken(JsonToken.VALUE_NULL)) {
            if (_skipNulls) {
                return;
            }
            value = _nullProvider.getNullValue(ctxt);
        } else if (_valueTypeDeserializer == null) {
            value = _valueDeserializer.deserialize(p, ctxt);
        } else {
            value = _valueDeserializer.deserializeWithType(p, ctxt, _valueTypeDeserializer);
        }
        try {
            _field.set(instance, value);
        } catch (Exception e) {
            _throwAsIOE(p, e, value);
        }
    }

    @Override
    public Object deserializeSetAndReturn(@Initialized FieldProperty this, @Initialized JsonParser p,
    		@Initialized
    		DeserializationContext ctxt, @Initialized Object instance) throws IOException
    {
        Object value;
        if (p.hasToken(JsonToken.VALUE_NULL)) {
            if (_skipNulls) {
                return instance;
            }
            value = _nullProvider.getNullValue(ctxt);
        } else if (_valueTypeDeserializer == null) {
            value = _valueDeserializer.deserialize(p, ctxt);
        } else {
            value = _valueDeserializer.deserializeWithType(p, ctxt, _valueTypeDeserializer);
        }
        try {
            _field.set(instance, value);
        } catch (Exception e) {
            _throwAsIOE(p, e, value);
        }
        return instance;
    }

    @Override
    public void set(@Initialized FieldProperty this, @Initialized Object instance, @Initialized Object value) throws IOException
    {
        try {
            _field.set(instance, value);
        } catch (Exception e) {
            // 15-Sep-2015, tatu: How could we get a ref to JsonParser?
            _throwAsIOE(e, value);
        }
    }

    @Override
    public Object setAndReturn(@Initialized FieldProperty this, @Initialized Object instance, @Initialized Object value) throws IOException
    {
        try {
            _field.set(instance, value);
        } catch (Exception e) {
            // 15-Sep-2015, tatu: How could we get a ref to JsonParser?
            _throwAsIOE(e, value);
        }
        return instance;
    }

    /*
    /**********************************************************
    /* JDK serialization handling
    /**********************************************************
     */

    Object readResolve() {
        return new FieldProperty(this);
    }
}