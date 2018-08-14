package com.fasterxml.jackson.databind.deser.impl;

import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.Annotations;

/**
 * This concrete sub-class implements property that is set
 * using regular "setter" method.
 */
public final class MethodProperty
    extends SettableBeanProperty
{
    private static final long serialVersionUID = 1;

    protected final AnnotatedMethod _annotated;
    
    /**
     * Setter method for modifying property value; used for
     * "regular" method-accessible properties.
     */
    protected final transient Method _setter;

    /**
     * @since 2.9
     */
    final protected boolean _skipNulls;
    
    public MethodProperty(@Initialized BeanPropertyDefinition propDef,
            @Initialized
            JavaType type, @Initialized @Nullable TypeDeserializer typeDeser,
            @Initialized
            Annotations contextAnnotations, @Initialized AnnotatedMethod method)
    {
        super(propDef, type, typeDeser, contextAnnotations);
        _annotated = method;
        _setter = method.getAnnotated();
        _skipNulls = NullsConstantProvider.isSkipper(_nullProvider);
    }

    protected MethodProperty(@Initialized MethodProperty src, @Initialized JsonDeserializer<?> deser,
            @Initialized
            NullValueProvider nva) {
        super(src, deser, nva);
        _annotated = src._annotated;
        _setter = src._setter;
        _skipNulls = NullsConstantProvider.isSkipper(nva);
    }

    protected MethodProperty(@Initialized MethodProperty src, @Initialized PropertyName newName) {
        super(src, newName);
        _annotated = src._annotated;
        _setter = src._setter;
        _skipNulls = src._skipNulls;
    }

    /**
     * Constructor used for JDK Serialization when reading persisted object
     */
    protected MethodProperty(@Initialized MethodProperty src, @Initialized Method m) {
        super(src);
        _annotated = src._annotated;
        _setter = m;
        _skipNulls = src._skipNulls;
    }

    @Override
    public SettableBeanProperty withName(@Initialized MethodProperty this, @Initialized PropertyName newName) {
        return new MethodProperty(this, newName);
    }
    
    @Override
    public SettableBeanProperty withValueDeserializer(@Initialized MethodProperty this, @Initialized JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        return new MethodProperty(this, deser, _nullProvider);
    }

    @Override
    public SettableBeanProperty withNullProvider(@Initialized MethodProperty this, @Initialized NullValueProvider nva) {
        return new MethodProperty(this, _valueDeserializer, nva);
    }

    @Override
    public void fixAccess(@Initialized MethodProperty this, @Initialized DeserializationConfig config) {
        _annotated.fixAccess(
                config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
    }

    /*
    /**********************************************************
    /* BeanProperty impl
    /**********************************************************
     */
    
    @Override
    public @Nullable <A extends Annotation> A getAnnotation(@Initialized MethodProperty this, @Initialized Class<A> acls) {
        return (_annotated == null) ? null : _annotated.getAnnotation(acls);
    }

    @Override public AnnotatedMember getMember(@Initialized MethodProperty this) {  return _annotated; }

    /*
    /**********************************************************
    /* Overridden methods
    /**********************************************************
     */

    @Override
    @SuppressWarnings("nullness") // need to annotate JsonParser|
    public void deserializeAndSet(@Initialized MethodProperty this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt,
            @Initialized
            Object instance) throws IOException
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
            _setter.invoke(instance, value);
        } catch (Exception e) {
            _throwAsIOE(p, e, value);
        }
    }

    @Override
    @SuppressWarnings("nullness") // need to annotate JsonParser|
    public @Nullable Object deserializeSetAndReturn(@Initialized MethodProperty this, @Initialized JsonParser p,
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
            Object result = _setter.invoke(instance, value);
            return (result == null) ? instance : result;
        } catch (Exception e) {
            _throwAsIOE(p, e, value);
            return null;
        }
    }

    @Override
    public final void set(@Initialized MethodProperty this, @Initialized Object instance, @Initialized Object value) throws IOException
    {
        try {
            _setter.invoke(instance, value);
        } catch (Exception e) {
            // 15-Sep-2015, tatu: How coud we get a ref to JsonParser?
            _throwAsIOE(e, value);
        }
    }

    @Override
    public @Nullable Object setAndReturn(@Initialized MethodProperty this, @Initialized Object instance, @Initialized Object value) throws IOException
    {
        try {
            Object result = _setter.invoke(instance, value);
            return (result == null) ? instance : result;
        } catch (Exception e) {
            // 15-Sep-2015, tatu: How coud we get a ref to JsonParser?
            _throwAsIOE(e, value);
            return null;
        }
    }

    /*
    /**********************************************************
    /* JDK serialization handling
    /**********************************************************
     */

    Object readResolve() {
        return new MethodProperty(this, _annotated.getAnnotated());
    }
}
