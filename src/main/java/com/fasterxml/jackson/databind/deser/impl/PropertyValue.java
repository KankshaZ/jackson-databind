package com.fasterxml.jackson.databind.deser.impl;

import org.checkerframework.checker.initialization.qual.Initialized;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.deser.SettableAnyProperty;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;

/**
 * Base class for property values that need to be buffered during
 * deserialization.
 */
public abstract class PropertyValue
{
    public final @Initialized PropertyValue next;

    /**
     * Value to assign when POJO has been instantiated.
     */
    public final @Initialized Object value;
    
    protected PropertyValue(PropertyValue next, Object value)
    {
        this.next = next;
        this.value = value;
    }

    /**
     * Method called to assign stored value of this property to specified
     * bean instance
     */
    public abstract void assign(@Initialized Object bean)
        throws IOException, JsonProcessingException;

    /*
    /**********************************************************
    /* Concrete property value classes
    /**********************************************************
     */

    /**
     * Property value that used when assigning value to property using
     * a setter method or direct field access.
     */
    final static class Regular
        extends PropertyValue
    {
        final @Initialized SettableBeanProperty _property;
        
        public Regular(@Initialized PropertyValue next, @Initialized Object value,
                       @Initialized
                       SettableBeanProperty prop)
        {
            super(next, value);
            _property = prop;
        }

        @Override
        public void assign(PropertyValue.@Initialized Regular this, @Initialized Object bean)
            throws IOException, JsonProcessingException
        {
            _property.set(bean, value);
        }
    }
    
    /**
     * Property value type used when storing entries to be added
     * to a POJO using "any setter" (method that takes name and
     * value arguments, allowing setting multiple different
     * properties using single method).
     */
    final static class Any
        extends PropertyValue
    {
        final @Initialized SettableAnyProperty _property;
        final @Initialized String _propertyName;
        
        public Any(@Initialized PropertyValue next, @Initialized Object value,
                   @Initialized
                   SettableAnyProperty prop,
                   @Initialized
                   String propName)
        {
            super(next, value);
            _property = prop;
            _propertyName = propName;
        }

        @Override
        public void assign(PropertyValue.@Initialized Any this, @Initialized Object bean)
            throws IOException, JsonProcessingException
        {
            _property.set(bean, _propertyName, value);
        }
    }

    /**
     * Property value type used when storing entries to be added
     * to a Map.
     */
    final static class Map
        extends PropertyValue
    {
        final @Initialized Object _key;
        
        public Map(@Initialized PropertyValue next, @Initialized Object value, @Initialized Object key)
        {
            super(next, value);
            _key = key;
        }

        @SuppressWarnings("unchecked") 
        @Override
        public void assign(PropertyValue.@Initialized Map this, @Initialized Object bean)
            throws IOException, JsonProcessingException
        {
            ((java.util.Map<Object,Object>) bean).put(_key, value);
        }
    }
}
