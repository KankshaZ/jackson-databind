package com.fasterxml.jackson.databind.deser.impl;

import org.checkerframework.checker.initialization.qual.Initialized;
import java.io.IOException;
import java.lang.annotation.Annotation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId.Referring;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;

public class ObjectIdReferenceProperty extends SettableBeanProperty
{
    private static final @Initialized long serialVersionUID = 1L;

    private final @Initialized SettableBeanProperty _forward;

    public ObjectIdReferenceProperty(@Initialized SettableBeanProperty forward, @Initialized ObjectIdInfo objectIdInfo)
    {
        super(forward);
        _forward = forward;
        _objectIdInfo = objectIdInfo;
    }

    public ObjectIdReferenceProperty(@Initialized ObjectIdReferenceProperty src, @Initialized JsonDeserializer<?> deser,
            @Initialized
            NullValueProvider nva)
    {
        super(src, deser, nva);
        _forward = src._forward;
        _objectIdInfo = src._objectIdInfo;
    }

    public ObjectIdReferenceProperty(@Initialized ObjectIdReferenceProperty src, @Initialized PropertyName newName)
    {
        super(src, newName);
        _forward = src._forward;
        _objectIdInfo = src._objectIdInfo;
    }

    @Override
    public SettableBeanProperty withName(@Initialized ObjectIdReferenceProperty this, @Initialized PropertyName newName) {
        return new ObjectIdReferenceProperty(this, newName);
    }

    @Override
    public SettableBeanProperty withValueDeserializer(@Initialized ObjectIdReferenceProperty this, @Initialized JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        return new ObjectIdReferenceProperty(this, deser, _nullProvider);
    }

    @Override
    public SettableBeanProperty withNullProvider(@Initialized ObjectIdReferenceProperty this, @Initialized NullValueProvider nva) {
        return new ObjectIdReferenceProperty(this, _valueDeserializer, nva);
    }
    
    @Override
    public void fixAccess(@Initialized ObjectIdReferenceProperty this, @Initialized DeserializationConfig config) {
        if (_forward != null) {
            _forward.fixAccess(config);
        }
    }

    @Override
    public <A extends Annotation> A getAnnotation(@Initialized ObjectIdReferenceProperty this, @Initialized Class<A> acls) {
        return _forward.getAnnotation(acls);
    }

    @Override
    public AnnotatedMember getMember(@Initialized ObjectIdReferenceProperty this) {
        return _forward.getMember();
    }

    @Override
    public int getCreatorIndex(@Initialized ObjectIdReferenceProperty this) {
        return _forward.getCreatorIndex();
    }

    @Override
    public void deserializeAndSet(@Initialized ObjectIdReferenceProperty this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt, @Initialized Object instance) throws IOException {
        deserializeSetAndReturn(p, ctxt, instance);
    }

    @Override
    public Object deserializeSetAndReturn(@Initialized ObjectIdReferenceProperty this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt, @Initialized Object instance) throws IOException
    {
        try {
            return setAndReturn(instance, deserialize(p, ctxt));
        } catch (UnresolvedForwardReference reference) {
            boolean usingIdentityInfo = (_objectIdInfo != null) || (_valueDeserializer.getObjectIdReader() != null);
            if (!usingIdentityInfo) {
                throw JsonMappingException.from(p, "Unresolved forward reference but no identity info", reference);
            }
            reference.getRoid().appendReferring(new PropertyReferring(this, reference, _type.getRawClass(), instance));
            return null;
        }
    }

    @Override
    public void set(@Initialized ObjectIdReferenceProperty this, @Initialized Object instance, @Initialized Object value) throws IOException {
        _forward.set(instance, value);
    }

    @Override
    public Object setAndReturn(@Initialized ObjectIdReferenceProperty this, @Initialized Object instance, @Initialized Object value) throws IOException {
        return _forward.setAndReturn(instance, value);
    }

    public final static class PropertyReferring extends Referring {
        private final @Initialized ObjectIdReferenceProperty _parent;
        public final @Initialized Object _pojo;

        public PropertyReferring(@Initialized ObjectIdReferenceProperty parent,
                @Initialized
                UnresolvedForwardReference ref, @Initialized Class<?> type, @Initialized Object ob)
        {
            super(ref, type);
            _parent = parent;
            _pojo = ob;
        }

        @Override
        public void handleResolvedForwardReference(ObjectIdReferenceProperty.@Initialized PropertyReferring this, @Initialized Object id, @Initialized Object value) throws IOException
        {
            if (!hasId(id)) {
                throw new IllegalArgumentException("Trying to resolve a forward reference with id [" + id
                        + "] that wasn't previously seen as unresolved.");
            }
            _parent.set(_pojo, value);
        }
    }
}
