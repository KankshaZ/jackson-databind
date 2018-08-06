package com.fasterxml.jackson.databind.deser.impl;

import org.checkerframework.checker.initialization.qual.Initialized;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Special bogus "serializer" that will throw
 * {@link JsonMappingException} if an attempt is made to deserialize
 * a value. This is used as placeholder to avoid NPEs for uninitialized
 * structured serializers or handlers.
 */
public class FailingDeserializer extends StdDeserializer<Object>
{
    private static final @Initialized long serialVersionUID = 1L;

    protected final @Initialized String _message;

    public FailingDeserializer(@Initialized String m) {
        super(Object.class);
        _message = m;
    }
    
    @Override
    public Object deserialize(@Initialized FailingDeserializer this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt) throws IOException {
        ctxt.reportInputMismatch(this, _message);
        return null;
    }
}
