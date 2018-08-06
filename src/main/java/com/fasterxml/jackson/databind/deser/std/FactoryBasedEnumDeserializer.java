package com.fasterxml.jackson.databind.deser.std;

import org.checkerframework.checker.initialization.qual.Initialized;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.PropertyBasedCreator;
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.ClassUtil;

/**
 * Deserializer that uses a single-String static factory method
 * for locating Enum values by String id.
 * 
 * @since 2.8 (as stand-alone class; was static inner class of {@link EnumDeserializer}
 */
class FactoryBasedEnumDeserializer
    extends StdDeserializer<Object>
    implements ContextualDeserializer
{
    private static final @Initialized long serialVersionUID = 1;

    // Marker type; null if String expected; otherwise numeric wrapper
    protected final @Initialized JavaType _inputType;
    protected final @Initialized boolean _hasArgs;
    protected final @Initialized AnnotatedMethod _factory;
    protected final @Initialized JsonDeserializer<?> _deser;
    protected final @Initialized ValueInstantiator _valueInstantiator;
    protected final SettableBeanProperty @Initialized [] _creatorProps;

    /**
     * Lazily instantiated property-based creator.
     *
     * @since 2.8
     */
    private transient @Initialized PropertyBasedCreator _propCreator;
    
    public FactoryBasedEnumDeserializer(@Initialized Class<?> cls, @Initialized AnnotatedMethod f, @Initialized JavaType paramType,
            @Initialized
            ValueInstantiator valueInstantiator, SettableBeanProperty @Initialized [] creatorProps)
    {
        super(cls);
        _factory = f;
        _hasArgs = true;
        // We'll skip case of `String`, as well as no type (zero-args): 
        _inputType = paramType.hasRawClass(String.class) ? null : paramType;
        _deser = null;
        _valueInstantiator = valueInstantiator;
        _creatorProps = creatorProps;
    }

    /**
     * @since 2.8
     */
    public FactoryBasedEnumDeserializer(@Initialized Class<?> cls, @Initialized AnnotatedMethod f)
    {
        super(cls);
        _factory = f;
        _hasArgs = false;
        _inputType = null;
        _deser = null;
        _valueInstantiator = null;
        _creatorProps = null;
    }

    protected FactoryBasedEnumDeserializer(@Initialized FactoryBasedEnumDeserializer base,
            @Initialized
            JsonDeserializer<?> deser) {
        super(base._valueClass);
        _inputType = base._inputType;
        _factory = base._factory;
        _hasArgs = base._hasArgs;
        _valueInstantiator = base._valueInstantiator;
        _creatorProps = base._creatorProps;

        _deser = deser;
    }

    @Override
    public JsonDeserializer<?> createContextual(@Initialized FactoryBasedEnumDeserializer this, @Initialized DeserializationContext ctxt,
            @Initialized
            BeanProperty property)
        throws JsonMappingException
    {
        if ((_deser == null) && (_inputType != null) && (_creatorProps == null)) {
            return new FactoryBasedEnumDeserializer(this,
                    ctxt.findContextualValueDeserializer(_inputType, property));
        }
        return this;
    }

    @Override // since 2.9
    public Boolean supportsUpdate(@Initialized FactoryBasedEnumDeserializer this, @Initialized DeserializationConfig config) {
        return Boolean.FALSE;
    }

    @Override
    public Object deserialize(@Initialized FactoryBasedEnumDeserializer this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt) throws IOException
    {
        Object value = null;
        if (_deser != null) {
            value = _deser.deserialize(p, ctxt);
        } else if (_hasArgs) {
            JsonToken curr = p.getCurrentToken();
            //There can be a JSON object passed for deserializing an Enum,
            //the below case handles it.
            if (curr == JsonToken.VALUE_STRING || curr == JsonToken.FIELD_NAME) {
                value = p.getText();
            } else if ((_creatorProps != null) && p.isExpectedStartObjectToken()) {
                if (_propCreator == null) {
                    _propCreator = PropertyBasedCreator.construct(ctxt, _valueInstantiator, _creatorProps,
                            ctxt.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
                }
                p.nextToken();
                return deserializeEnumUsingPropertyBased(p, ctxt, _propCreator);
            } else {
                value = p.getValueAsString();
            }
        } else { // zero-args; just skip whatever value there may be
            p.skipChildren();
            try {
                return _factory.call();
            } catch (Exception e) {
                Throwable t = ClassUtil.throwRootCauseIfIOE(e);
                return ctxt.handleInstantiationProblem(_valueClass, null, t);
            }
        }
        try {
            return _factory.callOnWith(_valueClass, value);
        } catch (Exception e) {
            Throwable t = ClassUtil.throwRootCauseIfIOE(e);
            // [databind#1642]:
            if (ctxt.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL) &&
                    t instanceof IllegalArgumentException) {
                return null;
            }
            return ctxt.handleInstantiationProblem(_valueClass, value, t);
        }
    }

    @Override
    public Object deserializeWithType(@Initialized FactoryBasedEnumDeserializer this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt, @Initialized TypeDeserializer typeDeserializer) throws IOException {
        if (_deser == null) { // String never has type info
            return deserialize(p, ctxt);
        }
        return typeDeserializer.deserializeTypedFromAny(p, ctxt);
    }
    
    // Method to deserialize the Enum using property based methodology
    protected Object deserializeEnumUsingPropertyBased(final @Initialized JsonParser p, final @Initialized DeserializationContext ctxt,
    		final @Initialized PropertyBasedCreator creator) throws IOException
    {
        PropertyValueBuffer buffer = creator.startBuilding(p, ctxt, null);
    
        JsonToken t = p.getCurrentToken();
        for (; t == JsonToken.FIELD_NAME; t = p.nextToken()) {
            String propName = p.getCurrentName();
            p.nextToken(); // to point to value
    
            SettableBeanProperty creatorProp = creator.findCreatorProperty(propName);
            if (creatorProp != null) {
                buffer.assignParameter(creatorProp, _deserializeWithErrorWrapping(p, ctxt, creatorProp));
                continue;
            }
            if (buffer.readIdProperty(propName)) {
                continue;
            }
        }
        return creator.build(ctxt, buffer);
    }

    // ************ Got the below methods from BeanDeserializer ********************//

    protected final Object _deserializeWithErrorWrapping(@Initialized JsonParser p, @Initialized DeserializationContext ctxt,
            @Initialized
            SettableBeanProperty prop) throws IOException
    {
        try {
            return prop.deserialize(p, ctxt);
        } catch (Exception e) {
            return wrapAndThrow(e, handledType(), prop.getName(), ctxt);
        }
    }

    protected Object wrapAndThrow(@Initialized Throwable t, @Initialized Object bean, @Initialized String fieldName, @Initialized DeserializationContext ctxt)
            throws IOException
    {
        throw JsonMappingException.wrapWithPath(throwOrReturnThrowable(t, ctxt), bean, fieldName);
    }

    private Throwable throwOrReturnThrowable(@Initialized Throwable t, @Initialized DeserializationContext ctxt) throws IOException
    {
        t = ClassUtil.getRootCause(t);
        // Errors to be passed as is
        ClassUtil.throwIfError(t);
        boolean wrap = (ctxt == null) || ctxt.isEnabled(DeserializationFeature.WRAP_EXCEPTIONS);
        // Ditto for IOExceptions; except we may want to wrap JSON exceptions
        if (t instanceof IOException) {
            if (!wrap || !(t instanceof JsonProcessingException)) {
                throw (IOException) t;
            }
        } else if (!wrap) {
            ClassUtil.throwIfRTE(t);
        }
        return t;
    }
}
