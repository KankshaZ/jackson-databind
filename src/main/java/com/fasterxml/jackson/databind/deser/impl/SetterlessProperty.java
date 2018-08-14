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
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.Annotations;

/**
 * This concrete sub-class implements Collection or Map property that is
 * indirectly by getting the property value and directly modifying it.
 */
public final class SetterlessProperty
    extends SettableBeanProperty
{
    private static final long serialVersionUID = 1L;

    protected final AnnotatedMethod _annotated;

    /**
     * Get method for accessing property value used to access property
     * (of Collection or Map type) to modify.
     */
    protected final Method _getter;

    public SetterlessProperty(@Initialized BeanPropertyDefinition propDef, @Initialized JavaType type,
            @Initialized
            @Nullable
            TypeDeserializer typeDeser, @Initialized Annotations contextAnnotations, @Initialized AnnotatedMethod method)
    {
        super(propDef, type, typeDeser, contextAnnotations);
        _annotated = method;
        _getter = method.getAnnotated();
    }

    protected SetterlessProperty(@Initialized SetterlessProperty src, @Initialized JsonDeserializer<?> deser,
            @Initialized
            NullValueProvider nva) {
        super(src, deser, nva);
        _annotated = src._annotated;
        _getter = src._getter;
    }

    protected SetterlessProperty(@Initialized SetterlessProperty src, @Initialized PropertyName newName) {
        super(src, newName);
        _annotated = src._annotated;
        _getter = src._getter;
    }

    @Override
    public SettableBeanProperty withName(@Initialized SetterlessProperty this, @Initialized PropertyName newName) {
        return new SetterlessProperty(this, newName);
    }

    @Override
    public SettableBeanProperty withValueDeserializer(@Initialized SetterlessProperty this, @Initialized JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        return new SetterlessProperty(this, deser, _nullProvider);
    }

    @Override
    public SettableBeanProperty withNullProvider(@Initialized SetterlessProperty this, @Initialized NullValueProvider nva) {
        return new SetterlessProperty(this, _valueDeserializer, nva);
    }

    @Override
    public void fixAccess(@Initialized SetterlessProperty this, @Initialized DeserializationConfig config) {
        _annotated.fixAccess(
                config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
    }

    /*
    /**********************************************************
    /* BeanProperty impl
    /**********************************************************
     */
    
    @Override
    public <A extends Annotation> A getAnnotation(@Initialized SetterlessProperty this, @Initialized Class<A> acls) {
        return _annotated.getAnnotation(acls);
    }

    @Override public AnnotatedMember getMember(@Initialized SetterlessProperty this) {  return _annotated; }

    /*
    /**********************************************************
    /* Overridden methods
    /**********************************************************
     */
    
    @Override
    public final void deserializeAndSet(@Initialized SetterlessProperty this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt,
            @Initialized
            Object instance) throws IOException
    {
        JsonToken t = p.getCurrentToken();
        if (t == JsonToken.VALUE_NULL) {
            // Hmmh. Is this a problem? We won't be setting anything, so it's
            // equivalent of empty Collection/Map in this case
            return;
        }
        // For [databind#501] fix we need to implement this but:
        if (_valueTypeDeserializer != null) {
            ctxt.reportBadDefinition(getType(), String.format(
                    "Problem deserializing 'setterless' property (\"%s\"): no way to handle typed deser with setterless yet",
                    getName()));
//            return _valueDeserializer.deserializeWithType(p, ctxt, _valueTypeDeserializer);
        }
        // Ok: then, need to fetch Collection/Map to modify:
        Object toModify;
        try {
            toModify = _getter.invoke(instance, (Object[]) null);
        } catch (Exception e) {
            _throwAsIOE(p, e);
            return; // never gets here
        }
        // Note: null won't work, since we can't then inject anything in. At least
        // that's not good in common case. However, theoretically the case where
        // we get JSON null might be compatible. If so, implementation could be changed.
        if (toModify == null) {
            ctxt.reportBadDefinition(getType(), String.format(
                    "Problem deserializing 'setterless' property '%s': get method returned null",
                    getName()));
        }
        _valueDeserializer.deserialize(p, ctxt, toModify);
    }

    @Override
    public Object deserializeSetAndReturn(@Initialized SetterlessProperty this, @Initialized JsonParser p,
    		@Initialized
    		DeserializationContext ctxt, @Initialized Object instance) throws IOException
    {
        deserializeAndSet(p, ctxt, instance);
        return instance;
    }

    @Override
    public final void set(@Initialized SetterlessProperty this, @Initialized Object instance, @Initialized Object value) throws IOException {
        throw new UnsupportedOperationException("Should never call `set()` on setterless property ('"+getName()+"')");
    }

    @Override
    public Object setAndReturn(@Initialized SetterlessProperty this, @Initialized Object instance, @Initialized Object value) throws IOException
    {
        set(instance, value);
        return instance;
    }
}