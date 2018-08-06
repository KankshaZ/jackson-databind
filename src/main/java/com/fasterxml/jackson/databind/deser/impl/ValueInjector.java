package com.fasterxml.jackson.databind.deser.impl;

import org.checkerframework.checker.initialization.qual.Initialized;
import java.io.IOException;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;

/**
 * Class that encapsulates details of value injection that occurs before
 * deserialization of a POJO. Details include information needed to find
 * injectable value (logical id) as well as method used for assigning
 * value (setter or field)
 */
public class ValueInjector
    extends BeanProperty.Std
{
    private static final @Initialized long serialVersionUID = 1L;

    /**
     * Identifier used for looking up value to inject
     */
    protected final @Initialized Object _valueId;

    public ValueInjector(@Initialized PropertyName propName, @Initialized JavaType type,
            @Initialized
            AnnotatedMember mutator, @Initialized Object valueId)
    {
        super(propName, type, null, mutator, PropertyMetadata.STD_OPTIONAL);
        _valueId = valueId;
    }

    /**
     * @deprecated in 2.9 (remove from 3.0)
     */
    @Deprecated // see [databind#1835]
    public ValueInjector(PropertyName propName, JavaType type,
            com.fasterxml.jackson.databind.util.Annotations contextAnnotations, // removed from later versions
            AnnotatedMember mutator, Object valueId)
    {
        this(propName, type, mutator, valueId);
    }

    public Object findValue(@Initialized DeserializationContext context, @Initialized Object beanInstance)
        throws JsonMappingException
    {
        return context.findInjectableValue(_valueId, this, beanInstance);
    }
    
    public void inject(@Initialized DeserializationContext context, @Initialized Object beanInstance)
        throws IOException
    {
        _member.setValue(beanInstance, findValue(context, beanInstance));
    }
}