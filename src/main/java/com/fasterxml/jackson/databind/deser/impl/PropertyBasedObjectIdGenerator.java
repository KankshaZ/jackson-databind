package com.fasterxml.jackson.databind.deser.impl;

import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.Nullable;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

// Simple placeholder
public class PropertyBasedObjectIdGenerator
	extends ObjectIdGenerators.PropertyGenerator
{
    private static final long serialVersionUID = 1L;

    public PropertyBasedObjectIdGenerator(@Initialized Class<?> scope) {
        super(scope);
    }
    
    @Override
    public Object generateId(@Initialized PropertyBasedObjectIdGenerator this, @Initialized Object forPojo) {
    	throw new UnsupportedOperationException();
    }

    @Override
    public ObjectIdGenerator<Object> forScope(@Initialized PropertyBasedObjectIdGenerator this, @Initialized Class<?> scope) {
        return (scope == _scope) ? this : new PropertyBasedObjectIdGenerator(scope);
    }

    @Override
    public ObjectIdGenerator<Object> newForSerialization(@Initialized PropertyBasedObjectIdGenerator this, @Initialized Object context) {
        return this;
    }

    @Override
    @SuppressWarnings("nullness") //need to annotate com.fasterxml.jackson.annotation.ObjectIdGenerator
    public com.fasterxml.jackson.annotation.ObjectIdGenerator.@Nullable IdKey key(@Initialized PropertyBasedObjectIdGenerator this, @Initialized Object key) {
        if (key == null) {
            return null;
        }
        // should we use general type for all; or type of property itself?
        return new IdKey(getClass(), _scope, key);
    }

}
