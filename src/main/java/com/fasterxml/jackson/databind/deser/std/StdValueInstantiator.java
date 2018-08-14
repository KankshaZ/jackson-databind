package com.fasterxml.jackson.databind.deser.std;

import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.util.ClassUtil;

/**
 * Default {@link ValueInstantiator} implementation, which supports
 * Creator methods that can be indicated by standard Jackson
 * annotations.
 */
@JacksonStdImpl
public class StdValueInstantiator
    extends ValueInstantiator
    implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Type of values that are instantiated; used
     * for error reporting purposes.
     */
    protected final String _valueTypeDesc;

    /**
     * @since 2.8
     */
    protected final Class<?> _valueClass;

    // // // Default (no-args) construction

    /**
     * Default (no-argument) constructor to use for instantiation
     * (with {@link #createUsingDefault})
     */
    protected @Nullable AnnotatedWithParams _defaultCreator;

    // // // With-args (property-based) construction

    protected @Nullable AnnotatedWithParams _withArgsCreator;
    protected SettableBeanProperty @Initialized @Nullable [] _constructorArguments;

    // // // Delegate construction
    
    protected @Nullable JavaType _delegateType;
    protected @Nullable AnnotatedWithParams _delegateCreator;
    protected SettableBeanProperty @Initialized @Nullable [] _delegateArguments;

    // // // Array delegate construction

    protected @Nullable JavaType _arrayDelegateType;
    protected @Nullable AnnotatedWithParams _arrayDelegateCreator;
    protected SettableBeanProperty @Initialized @Nullable [] _arrayDelegateArguments;
    
    // // // Scalar construction

    protected @Nullable AnnotatedWithParams _fromStringCreator;
    protected @Nullable AnnotatedWithParams _fromIntCreator;
    protected @Nullable AnnotatedWithParams _fromLongCreator;
    protected @Nullable AnnotatedWithParams _fromDoubleCreator;
    protected @Nullable AnnotatedWithParams _fromBooleanCreator;

    // // // Incomplete creator
    protected @Nullable AnnotatedParameter  _incompleteParameter;
    
    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    /**
     * @deprecated Since 2.7 use constructor that takes {@link JavaType} instead
     */
    @Deprecated
    public StdValueInstantiator(DeserializationConfig config, Class<?> valueType) {
        _valueTypeDesc = ClassUtil.nameOf(valueType);
        _valueClass = (valueType == null) ? Object.class : valueType;
    }

    public StdValueInstantiator(@Initialized DeserializationConfig config, @Initialized JavaType valueType) {
        _valueTypeDesc = (valueType == null) ? "UNKNOWN TYPE" : valueType.toString();
        _valueClass = (valueType == null) ? Object.class : valueType.getRawClass();
    }

    /**
     * Copy-constructor that sub-classes can use when creating new instances
     * by fluent-style construction
     */
    protected StdValueInstantiator(StdValueInstantiator src)
    {
        _valueTypeDesc = src._valueTypeDesc;
        _valueClass = src._valueClass;

        _defaultCreator = src._defaultCreator;

        _constructorArguments = src._constructorArguments;
        _withArgsCreator = src._withArgsCreator;

        _delegateType = src._delegateType;
        _delegateCreator = src._delegateCreator;
        _delegateArguments = src._delegateArguments;

        _arrayDelegateType = src._arrayDelegateType;
        _arrayDelegateCreator = src._arrayDelegateCreator;
        _arrayDelegateArguments = src._arrayDelegateArguments;
        
        _fromStringCreator = src._fromStringCreator;
        _fromIntCreator = src._fromIntCreator;
        _fromLongCreator = src._fromLongCreator;
        _fromDoubleCreator = src._fromDoubleCreator;
        _fromBooleanCreator = src._fromBooleanCreator;
    }

    /**
     * Method for setting properties related to instantiating values
     * from JSON Object. We will choose basically only one approach (out of possible
     * three), and clear other properties
     */
    public void configureFromObjectSettings(@Initialized AnnotatedWithParams defaultCreator,
            @Initialized
            AnnotatedWithParams delegateCreator, @Initialized @Nullable JavaType delegateType, SettableBeanProperty @Initialized @Nullable [] delegateArgs,
            @Initialized
            AnnotatedWithParams withArgsCreator, SettableBeanProperty @Initialized @Nullable [] constructorArgs)
    {
        _defaultCreator = defaultCreator;
        _delegateCreator = delegateCreator;
        _delegateType = delegateType;
        _delegateArguments = delegateArgs;
        _withArgsCreator = withArgsCreator;
        _constructorArguments = constructorArgs;
    }

    public void configureFromArraySettings(
            @Initialized
            AnnotatedWithParams arrayDelegateCreator,
            @Initialized @Nullable
            JavaType arrayDelegateType,
            SettableBeanProperty @Initialized @Nullable [] arrayDelegateArgs)
    {
        _arrayDelegateCreator = arrayDelegateCreator;
        _arrayDelegateType = arrayDelegateType;
        _arrayDelegateArguments = arrayDelegateArgs;
    }

    public void configureFromStringCreator(@Initialized AnnotatedWithParams creator) {
        _fromStringCreator = creator;
    }

    public void configureFromIntCreator(@Initialized AnnotatedWithParams creator) {
        _fromIntCreator = creator;
    }

    public void configureFromLongCreator(@Initialized AnnotatedWithParams creator) {
        _fromLongCreator = creator;
    }

    public void configureFromDoubleCreator(@Initialized AnnotatedWithParams creator) {
        _fromDoubleCreator = creator;
    }

    public void configureFromBooleanCreator(@Initialized AnnotatedWithParams creator) {
        _fromBooleanCreator = creator;
    }

    public void configureIncompleteParameter(@Initialized @Nullable AnnotatedParameter parameter) {
        _incompleteParameter = parameter;
    }
    
    /*
    /**********************************************************
    /* Public API implementation; metadata
    /**********************************************************
     */

    @Override
    public String getValueTypeDesc(@Initialized StdValueInstantiator this) {
        return _valueTypeDesc;
    }

    @Override
    public Class<?> getValueClass(@Initialized StdValueInstantiator this) {
        return _valueClass;
    }

    @Override
    public boolean canCreateFromString(@Initialized StdValueInstantiator this) {
        return (_fromStringCreator != null);
    }

    @Override
    public boolean canCreateFromInt(@Initialized StdValueInstantiator this) {
        return (_fromIntCreator != null);
    }

    @Override
    public boolean canCreateFromLong(@Initialized StdValueInstantiator this) {
        return (_fromLongCreator != null);
    }

    @Override
    public boolean canCreateFromDouble(@Initialized StdValueInstantiator this) {
        return (_fromDoubleCreator != null);
    }

    @Override
    public boolean canCreateFromBoolean(@Initialized StdValueInstantiator this) {
        return (_fromBooleanCreator != null);
    }

    @Override
    public boolean canCreateUsingDefault(@Initialized StdValueInstantiator this) {
        return (_defaultCreator != null);
    }

    @Override
    public boolean canCreateUsingDelegate(@Initialized StdValueInstantiator this) {
        return (_delegateType != null);
    }

    @Override
    public boolean canCreateUsingArrayDelegate(@Initialized StdValueInstantiator this) {
        return (_arrayDelegateType != null);
    }

    @Override
    public boolean canCreateFromObjectWith(@Initialized StdValueInstantiator this) {
        return (_withArgsCreator != null);
    }

    @Override
    public boolean canInstantiate(@Initialized StdValueInstantiator this) {
        return canCreateUsingDefault()
                || canCreateUsingDelegate() || canCreateUsingArrayDelegate()
                || canCreateFromObjectWith() || canCreateFromString()
                || canCreateFromInt() || canCreateFromLong()
                || canCreateFromDouble() || canCreateFromBoolean();
    }

    @Override
    public @Nullable JavaType getDelegateType(@Initialized StdValueInstantiator this, @Initialized DeserializationConfig config) {
        return _delegateType;
    }

    @Override
    public @Nullable JavaType getArrayDelegateType(@Initialized StdValueInstantiator this, @Initialized DeserializationConfig config) {
        return _arrayDelegateType;
    }

    @Override
    public SettableBeanProperty @Nullable [] getFromObjectArguments(@Initialized StdValueInstantiator this, @Initialized DeserializationConfig config) {
        return _constructorArguments;
    }
    
    /*
    /**********************************************************
    /* Public API implementation; instantiation from JSON Object
    /**********************************************************
     */
    
    @Override
    @SuppressWarnings("nullness") // need to annotate DeserializationContext.java in parent folder
    public Object createUsingDefault(@Initialized StdValueInstantiator this, @Initialized DeserializationContext ctxt) throws IOException
    {
        if (_defaultCreator == null) { // sanity-check; caller should check
            return super.createUsingDefault(ctxt);
        }
        try {
            return _defaultCreator.call();
        } catch (Exception e) { // 19-Apr-2017, tatu: Let's not catch Errors, just Exceptions
            return ctxt.handleInstantiationProblem(_valueClass, null, rewrapCtorProblem(ctxt, e));
        }
    }

    @Override
    public Object createFromObjectWith(@Initialized StdValueInstantiator this, @Initialized DeserializationContext ctxt, Object @Initialized [] args) throws IOException
    {
        if (_withArgsCreator == null) { // sanity-check; caller should check
            return super.createFromObjectWith(ctxt, args);
        }
        try {
            return _withArgsCreator.call(args);
        } catch (Exception e) { // 19-Apr-2017, tatu: Let's not catch Errors, just Exceptions
            return ctxt.handleInstantiationProblem(_valueClass, args, rewrapCtorProblem(ctxt, e));
        }
    }

    @Override
    public Object createUsingDelegate(@Initialized StdValueInstantiator this, @Initialized DeserializationContext ctxt, @Initialized Object delegate) throws IOException
    {
        // 04-Oct-2016, tatu: Need delegation to work around [databind#1392]...
        if (_delegateCreator == null) {
            if (_arrayDelegateCreator != null) {
                return _createUsingDelegate(_arrayDelegateCreator, _arrayDelegateArguments, ctxt, delegate);
            }
        }
        return _createUsingDelegate(_delegateCreator, _delegateArguments, ctxt, delegate);
    }

    @Override
    public Object createUsingArrayDelegate(@Initialized StdValueInstantiator this, @Initialized DeserializationContext ctxt, @Initialized Object delegate) throws IOException
    {
        if (_arrayDelegateCreator == null) {
            if (_delegateCreator != null) { // sanity-check; caller should check
                // fallback to the classic delegate creator
                return createUsingDelegate(ctxt, delegate);
            }
        }
        return _createUsingDelegate(_arrayDelegateCreator, _arrayDelegateArguments, ctxt, delegate);
    }

    /*
    /**********************************************************
    /* Public API implementation; instantiation from JSON scalars
    /**********************************************************
     */

    @Override
    public @Nullable Object createFromString(@Initialized StdValueInstantiator this, @Initialized DeserializationContext ctxt, @Initialized String value) throws IOException
    {
        if (_fromStringCreator == null) {
            return _createFromStringFallbacks(ctxt, value);
        }
        try {
            return _fromStringCreator.call1(value);
        } catch (Throwable t) {
            return ctxt.handleInstantiationProblem(_fromStringCreator.getDeclaringClass(),
                    value, rewrapCtorProblem(ctxt, t));
        }
    }
    
    @Override
    public Object createFromInt(@Initialized StdValueInstantiator this, @Initialized DeserializationContext ctxt, @Initialized int value) throws IOException
    {
        // First: "native" int methods work best:
        if (_fromIntCreator != null) {
            Object arg = Integer.valueOf(value);
            try {
                return _fromIntCreator.call1(arg);
            } catch (Throwable t0) {
                return ctxt.handleInstantiationProblem(_fromIntCreator.getDeclaringClass(),
                        arg, rewrapCtorProblem(ctxt, t0));
            }
        }
        // but if not, can do widening conversion
        if (_fromLongCreator != null) {
            Object arg = Long.valueOf(value);
            try {
                return _fromLongCreator.call1(arg);
            } catch (Throwable t0) {
                return ctxt.handleInstantiationProblem(_fromLongCreator.getDeclaringClass(),
                        arg, rewrapCtorProblem(ctxt, t0));
            }
        }
        return super.createFromInt(ctxt, value);
    }

    @Override
    public Object createFromLong(@Initialized StdValueInstantiator this, @Initialized DeserializationContext ctxt, @Initialized long value) throws IOException
    {
        if (_fromLongCreator == null) {
            return super.createFromLong(ctxt, value);
        }
        Object arg = Long.valueOf(value);
        try {
            return _fromLongCreator.call1(arg);
        } catch (Throwable t0) {
            return ctxt.handleInstantiationProblem(_fromLongCreator.getDeclaringClass(),
                    arg, rewrapCtorProblem(ctxt, t0));
        }
    }

    @Override
    public Object createFromDouble(@Initialized StdValueInstantiator this, @Initialized DeserializationContext ctxt, @Initialized double value) throws IOException
    {
        if (_fromDoubleCreator == null) {
            return super.createFromDouble(ctxt, value);
        }
        Object arg = Double.valueOf(value);
        try {
            return _fromDoubleCreator.call1(arg);
        } catch (Throwable t0) {
            return ctxt.handleInstantiationProblem(_fromDoubleCreator.getDeclaringClass(),
                    arg, rewrapCtorProblem(ctxt, t0));
        }
    }

    @Override
    public Object createFromBoolean(@Initialized StdValueInstantiator this, @Initialized DeserializationContext ctxt, @Initialized boolean value) throws IOException
    {
        if (_fromBooleanCreator == null) {
            return super.createFromBoolean(ctxt, value);
        }
        final Boolean arg = Boolean.valueOf(value);
        try {
            return _fromBooleanCreator.call1(arg);
        } catch (Throwable t0) {
            return ctxt.handleInstantiationProblem(_fromBooleanCreator.getDeclaringClass(),
                    arg, rewrapCtorProblem(ctxt, t0));
        }
    }
    
    /*
    /**********************************************************
    /* Extended API: configuration mutators, accessors
    /**********************************************************
     */

    @Override
    public @Nullable AnnotatedWithParams getDelegateCreator(@Initialized StdValueInstantiator this) {
        return _delegateCreator;
    }

    @Override
    public @Nullable AnnotatedWithParams getArrayDelegateCreator(@Initialized StdValueInstantiator this) {
        return _arrayDelegateCreator;
    }

    @Override
    public @Nullable AnnotatedWithParams getDefaultCreator(@Initialized StdValueInstantiator this) {
        return _defaultCreator;
    }

    @Override
    public @Nullable AnnotatedWithParams getWithArgsCreator(@Initialized StdValueInstantiator this) {
        return _withArgsCreator;
    }

    @Override
    public @Nullable AnnotatedParameter getIncompleteParameter(@Initialized StdValueInstantiator this) {
        return _incompleteParameter;
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    /**
     * @deprecated Since 2.7 call either {@link #unwrapAndWrapException} or
     *  {@link #wrapAsJsonMappingException}
     */
    @Deprecated // since 2.7
    @SuppressWarnings("nullness") //need to annotated JsonMappingException in parent folder
    protected JsonMappingException wrapException(Throwable t)
    {
        // 05-Nov-2015, tatu: This used to always unwrap the whole exception, but now only
        //   does so if and until `JsonMappingException` is found.
        for (Throwable curr = t; curr != null; curr = curr.getCause()) {
            if (curr instanceof JsonMappingException) {
                return (JsonMappingException) curr;
            }
        }
        return new JsonMappingException(null,
                "Instantiation of "+getValueTypeDesc()+" value failed: "+t.getMessage(), t);
    }

    /**
     * @since 2.7
     */
    protected JsonMappingException unwrapAndWrapException(DeserializationContext ctxt, Throwable t)
    {
        // 05-Nov-2015, tatu: This used to always unwrap the whole exception, but now only
        //   does so if and until `JsonMappingException` is found.
        for (Throwable curr = t; curr != null; curr = curr.getCause()) {
            if (curr instanceof JsonMappingException) {
                return (JsonMappingException) curr;
            }
        }
        return ctxt.instantiationException(getValueClass(), t);
    }

    /**
     * @since 2.7
     */
    protected JsonMappingException wrapAsJsonMappingException(@Initialized DeserializationContext ctxt,
            @Initialized
            Throwable t)
    {
        // 05-Nov-2015, tatu: Only avoid wrapping if already a JsonMappingException
        if (t instanceof JsonMappingException) {
            return (JsonMappingException) t;
        }
        return ctxt.instantiationException(getValueClass(), t);
    }

    /**
     * @since 2.7
     */
    protected JsonMappingException rewrapCtorProblem(@Initialized DeserializationContext ctxt,
            @Initialized
            Throwable t)
    {
        // 05-Nov-2015, tatu: Seems like there are really only 2 useless wrapper errors/exceptions,
        //    so just peel those, and nothing else
        if ((t instanceof ExceptionInInitializerError) // from static initialization block
                || (t instanceof InvocationTargetException) // from constructor/method
                ) {
            Throwable cause = t.getCause();
            if (cause != null) {
                t = cause;
            }
        }
        return wrapAsJsonMappingException(ctxt, t);
    }

    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */
    @SuppressWarnings("nullness") // need to annotate findInjectableValue DeserializationContext.java in parent folder
    private Object _createUsingDelegate(
            @Initialized @Nullable
            AnnotatedWithParams delegateCreator,
            SettableBeanProperty @Initialized @Nullable [] delegateArguments,
            @Initialized
            DeserializationContext ctxt,
            @Initialized
            Object delegate)
            throws IOException
    {
        if (delegateCreator == null) { // sanity-check; caller should check
            throw new IllegalStateException("No delegate constructor for "+getValueTypeDesc());
        }
        try {
            // First simple case: just delegate, no injectables
            if (delegateArguments == null) {
                return delegateCreator.call1(delegate);
            }
            // And then the case with at least one injectable...
            final int len = delegateArguments.length;
            Object[] args = new Object[len];
            for (int i = 0; i < len; ++i) {
                SettableBeanProperty prop = delegateArguments[i];
                if (prop == null) { // delegate
                    args[i] = delegate;
                } else { // nope, injectable:
                    args[i] = ctxt.findInjectableValue(prop.getInjectableValueId(), prop, null);
                }
            }
            // and then try calling with full set of arguments
            return delegateCreator.call(args);
        } catch (Throwable t) {
            throw rewrapCtorProblem(ctxt, t);
        }
    }
}
