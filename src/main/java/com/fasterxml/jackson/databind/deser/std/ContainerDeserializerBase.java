package com.fasterxml.jackson.databind.deser.std;

import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.initialization.qual.FBCBottom;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.AccessPattern;
import com.fasterxml.jackson.databind.util.ClassUtil;

/**
 * Intermediate base deserializer class that adds more shared accessor
 * so that other classes can access information about contained (value) types
 */
@SuppressWarnings("serial")
public abstract class ContainerDeserializerBase<T>
    extends StdDeserializer<T>
    implements ValueInstantiator.Gettable // since 2.9
{
    protected final @Initialized JavaType _containerType;

    /**
     * Handler we need for dealing with nulls.
     *
     * @since 2.9
     */
    protected final @Initialized NullValueProvider _nullProvider;

    /**
     * Specific override for this instance (from proper, or global per-type overrides)
     * to indicate whether single value may be taken to mean an unwrapped one-element array
     * or not. If null, left to global defaults.
     *
     * @since 2.9 (demoted from sub-classes where added in 2.7)
     */
    protected final @Initialized Boolean _unwrapSingle;

    /**
     * Marker flag set if the <code>_nullProvider</code> indicates that all null
     * content values should be skipped (instead of being possibly converted).
     *
     * @since 2.9
     */
    protected final @Initialized boolean _skipNullValues;

    protected ContainerDeserializerBase(@Initialized JavaType selfType,
            @FBCBottom
            @Nullable
            NullValueProvider nuller, @FBCBottom @Nullable Boolean unwrapSingle) {
        super(selfType);
        _containerType = selfType;
        _unwrapSingle = unwrapSingle;
        _nullProvider = nuller;
        _skipNullValues = NullsConstantProvider.isSkipper(nuller);
    }

    protected ContainerDeserializerBase(JavaType selfType) {
        this(selfType, null, null);
    }

    /**
     * @since 2.9
     */
    protected ContainerDeserializerBase(ContainerDeserializerBase<?> base) {
        this(base, base._nullProvider, base._unwrapSingle);
    }

    /**
     * @since 2.9
     */
    protected ContainerDeserializerBase(@Initialized ContainerDeserializerBase<?> base,
            @Initialized
            NullValueProvider nuller, @Initialized Boolean unwrapSingle) {
        super(base._containerType);
        _containerType = base._containerType;
        _nullProvider = nuller;
        _unwrapSingle = unwrapSingle;
        _skipNullValues = NullsConstantProvider.isSkipper(nuller);
    }

    /*
    /**********************************************************
    /* Overrides
    /**********************************************************
     */

    @Override // since 2.9
    public JavaType getValueType(@Initialized ContainerDeserializerBase<T> this) { return _containerType; }
    
    @Override // since 2.9
    public Boolean supportsUpdate(@Initialized ContainerDeserializerBase<T> this, @Initialized DeserializationConfig config) {
        return Boolean.TRUE;
    }

    @Override
    public SettableBeanProperty findBackReference(@Initialized ContainerDeserializerBase<T> this, @Initialized String refName) {
        JsonDeserializer<Object> valueDeser = getContentDeserializer();
        if (valueDeser == null) {
            throw new IllegalArgumentException(String.format(
                    "Cannot handle managed/back reference '%s': type: container deserializer of type %s returned null for 'getContentDeserializer()'",
                    refName, getClass().getName()));
        }
        return valueDeser.findBackReference(refName);
    }

    /*
    /**********************************************************
    /* Extended API
    /**********************************************************
     */

    /**
     * Accessor for declared type of contained value elements; either exact
     * type, or one of its supertypes.
     */
    public JavaType getContentType() {
        if (_containerType == null) {
            return TypeFactory.unknownType(); // should never occur but...
        }
        return _containerType.getContentType();
    }

    /**
     * Accesor for deserializer use for deserializing content values.
     */
    public abstract JsonDeserializer<Object> getContentDeserializer();

    /**
     * @since 2.9
     */
    @Override
    public ValueInstantiator getValueInstantiator(@Initialized ContainerDeserializerBase<T> this) {
        return null;
    }

    @Override // since 2.9
    public AccessPattern getEmptyAccessPattern(@Initialized ContainerDeserializerBase<T> this) {
        // 02-Feb-2017, tatu: Empty containers are usually constructed as needed
        //   and may not be shared; for some deserializers this may be further refined.
        return AccessPattern.DYNAMIC;
    }
    
    @Override // since 2.9
    public Object getEmptyValue(@Initialized ContainerDeserializerBase<T> this, @Initialized DeserializationContext ctxt) throws JsonMappingException {
        ValueInstantiator vi = getValueInstantiator();
        if (vi == null || !vi.canCreateUsingDefault()) {
            JavaType type = getValueType();
            ctxt.reportBadDefinition(type,
                    String.format("Cannot create empty instance of %s, no default Creator", type));
        }
        try {
            return vi.createUsingDefault(ctxt);
        } catch (IOException e) {
            return ClassUtil.throwAsMappingException(ctxt, e);
        }
    }

    /*
    /**********************************************************
    /* Shared methods for sub-classes
    /**********************************************************
     */

    /**
     * Helper method called by various Map(-like) deserializers.
     */
    protected <BOGUS> BOGUS wrapAndThrow(@Initialized Throwable t, Object ref, String key) throws IOException
    {
        // to handle StackOverflow:
        while (t instanceof InvocationTargetException && t.getCause() != null) {
            t = t.getCause();
        }
        // Errors and "plain" IOExceptions to be passed as is
        ClassUtil.throwIfError(t);
        // ... except for mapping exceptions
        if (t instanceof IOException && !(t instanceof JsonMappingException)) {
            throw (IOException) t;
        }
        // for [databind#1141]
        throw JsonMappingException.wrapWithPath(t, ref,
                ClassUtil.nonNull(key, "N/A"));
    }
}
