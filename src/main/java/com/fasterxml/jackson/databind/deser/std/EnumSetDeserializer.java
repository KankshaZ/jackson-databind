package com.fasterxml.jackson.databind.deser.std;

import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.initialization.qual.FBCBottom;
import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

/**
 * Standard deserializer for {@link EnumSet}s.
 * <p>
 * Note: casting within this class is all messed up -- just could not figure out a way
 * to properly deal with recursive definition of "EnumSet&lt;K extends Enum&lt;K&gt;, V&gt;
 */
@SuppressWarnings("rawtypes")
public class EnumSetDeserializer
    extends StdDeserializer<EnumSet<?>>
    implements ContextualDeserializer
{
    private static final @Initialized long serialVersionUID = 1L; // since 2.5

    protected final @Initialized JavaType _enumType;

    protected final @Initialized Class<Enum> _enumClass;

    protected @Initialized JsonDeserializer<Enum<?>> _enumDeserializer;

    /**
     * Specific override for this instance (from proper, or global per-type overrides)
     * to indicate whether single value may be taken to mean an unwrapped one-element array
     * or not. If null, left to global defaults.
     *
     * @since 2.7
     */
    protected final @Initialized Boolean _unwrapSingle;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    @SuppressWarnings("unchecked" )
    public EnumSetDeserializer(@Initialized JavaType enumType, @FBCBottom @Nullable JsonDeserializer<?> deser)
    {
        super(EnumSet.class);
        _enumType = enumType;
        _enumClass = (Class<Enum>) enumType.getRawClass();
        // sanity check
        if (!_enumClass.isEnum()) {
            throw new IllegalArgumentException("Type "+enumType+" not Java Enum type");
        }
        _enumDeserializer = (JsonDeserializer<Enum<?>>) deser;
        _unwrapSingle = null;
    }

    /**
     * @since 2.7
     */
    @SuppressWarnings("unchecked" )
    protected EnumSetDeserializer(@Initialized EnumSetDeserializer base,
            @Initialized
            JsonDeserializer<?> deser, @Initialized Boolean unwrapSingle) {
        super(base);
        _enumType = base._enumType;
        _enumClass = base._enumClass;
        _enumDeserializer = (JsonDeserializer<Enum<?>>) deser;
        _unwrapSingle = unwrapSingle;
    }

    public EnumSetDeserializer withDeserializer(JsonDeserializer<?> deser) {
        if (_enumDeserializer == deser) {
            return this;
        }
        return new EnumSetDeserializer(this, deser, _unwrapSingle);
    }

    public EnumSetDeserializer withResolved(@Initialized JsonDeserializer<?> deser, @Initialized Boolean unwrapSingle) {
        if ((_unwrapSingle == unwrapSingle) && (_enumDeserializer == deser)) {
            return this;
        }
        return new EnumSetDeserializer(this, deser, unwrapSingle);
    }

    /**
     * Because of costs associated with constructing Enum resolvers,
     * let's cache instances by default.
     */
    @Override
    public boolean isCachable(@Initialized EnumSetDeserializer this) {
        // One caveat: content deserializer should prevent caching
        if (_enumType.getValueHandler() != null) {
            return false;
        }
        return true;
    }

    @Override // since 2.9
    public Boolean supportsUpdate(@Initialized EnumSetDeserializer this, @Initialized DeserializationConfig config) {
        return Boolean.TRUE;
    }

    @Override
    public JsonDeserializer<?> createContextual(@Initialized EnumSetDeserializer this, @Initialized DeserializationContext ctxt,
            @Initialized
            BeanProperty property) throws JsonMappingException
    {
        Boolean unwrapSingle = findFormatFeature(ctxt, property, EnumSet.class,
                JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        JsonDeserializer<?> deser = _enumDeserializer;
        if (deser == null) {
            deser = ctxt.findContextualValueDeserializer(_enumType, property);
        } else { // if directly assigned, probably not yet contextual, so:
            deser = ctxt.handleSecondaryContextualization(deser, property, _enumType);
        }
        return withResolved(deser, unwrapSingle);
    }

    /*
    /**********************************************************
    /* JsonDeserializer API
    /**********************************************************
     */

    @Override
    public EnumSet<?> deserialize(@Initialized EnumSetDeserializer this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt) throws IOException
    {
        EnumSet result = constructSet();
        // Ok: must point to START_ARRAY (or equivalent)
        if (!p.isExpectedStartArrayToken()) {
            return handleNonArray(p, ctxt, result);
        }
        return _deserialize(p, ctxt, result);
    }

    @Override
    public EnumSet<?> deserialize(@Initialized EnumSetDeserializer this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt,
            @Initialized
            EnumSet<?> result) throws IOException
    {
        // Ok: must point to START_ARRAY (or equivalent)
        if (!p.isExpectedStartArrayToken()) {
            return handleNonArray(p, ctxt, result);
        }
        return _deserialize(p, ctxt, result);
    }
    
    @SuppressWarnings("unchecked") 
    protected final EnumSet<?> _deserialize(@Initialized JsonParser p, @Initialized DeserializationContext ctxt,
            @Initialized
            EnumSet result) throws IOException
    {
        JsonToken t;

        try {
            while ((t = p.nextToken()) != JsonToken.END_ARRAY) {
                /* What to do with nulls? Fail or ignore? Fail, for now
                 * (note: would fail if we passed it to EnumDeserializer, too,
                 * but in general nulls should never be passed to non-container
                 * deserializers)
                 */
                if (t == JsonToken.VALUE_NULL) {
                    return (EnumSet<?>) ctxt.handleUnexpectedToken(_enumClass, p);
                }
                Enum<?> value = _enumDeserializer.deserialize(p, ctxt);
                /* 24-Mar-2012, tatu: As per [JACKSON-810], may actually get nulls;
                 *    but EnumSets don't allow nulls so need to skip.
                 */
                if (value != null) { 
                    result.add(value);
                }
            }
        } catch (Exception e) {
            throw JsonMappingException.wrapWithPath(e, result, result.size());
        }
        return result;
    }

    @Override
    public Object deserializeWithType(@Initialized EnumSetDeserializer this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt,
            @Initialized
            TypeDeserializer typeDeserializer)
        throws IOException, JsonProcessingException
    {
        return typeDeserializer.deserializeTypedFromArray(p, ctxt);
    }
    
    @SuppressWarnings("unchecked") 
    private EnumSet constructSet()
    {
        return EnumSet.noneOf(_enumClass);
    }

    @SuppressWarnings("unchecked") 
    protected EnumSet<?> handleNonArray(@Initialized JsonParser p, @Initialized DeserializationContext ctxt,
            @Initialized
            EnumSet result)
        throws IOException
    {
        boolean canWrap = (_unwrapSingle == Boolean.TRUE) ||
                ((_unwrapSingle == null) &&
                        ctxt.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));

        if (!canWrap) {
            return (EnumSet<?>) ctxt.handleUnexpectedToken(EnumSet.class, p);
        }
        // First: since `null`s not allowed, slightly simpler...
        if (p.hasToken(JsonToken.VALUE_NULL)) {
            return (EnumSet<?>) ctxt.handleUnexpectedToken(_enumClass, p);
        }
        try {
            Enum<?> value = _enumDeserializer.deserialize(p, ctxt);
            if (value != null) { 
                result.add(value);
            }
        } catch (Exception e) {
            throw JsonMappingException.wrapWithPath(e, result, result.size());
        }
        return result;
    }
}
