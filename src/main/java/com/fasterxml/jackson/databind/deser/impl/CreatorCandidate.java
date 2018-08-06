package com.fasterxml.jackson.databind.deser.impl;

import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.Nullable;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

public final class CreatorCandidate
{
    protected final @Initialized AnnotationIntrospector _intr;
    protected final @Initialized AnnotatedWithParams _creator;
    protected final @Initialized int _paramCount;
    protected final Param @Initialized [] _params;

    protected CreatorCandidate(@Initialized AnnotationIntrospector intr,
            @Initialized
            AnnotatedWithParams ct, Param @Initialized [] params, @Initialized int count) {
        _intr = intr;
        _creator = ct;
        _params = params;
        _paramCount = count;
    }

    public static CreatorCandidate construct(@Initialized AnnotationIntrospector intr,
            @Initialized
            AnnotatedWithParams creator, BeanPropertyDefinition @Initialized  @Nullable [] propDefs)
    {
        final int pcount = creator.getParameterCount();
        Param[] params = new Param[pcount];
        for (int i = 0; i < pcount; ++i) {
            AnnotatedParameter annParam = creator.getParameter(i);
            JacksonInject.Value injectId = intr.findInjectableValue(annParam);
            params[i] = new Param(annParam, (propDefs == null) ? null : propDefs[i], injectId);
        }
        return new CreatorCandidate(intr, creator, params, pcount);
    }

    public AnnotatedWithParams creator() { return _creator; }
    public int paramCount() { return _paramCount; }
    public JacksonInject.Value injection(@Initialized int i) { return _params[i].injection; }
    public AnnotatedParameter parameter(@Initialized int i) { return _params[i].annotated; }
    public BeanPropertyDefinition propertyDef(@Initialized int i) { return _params[i].propDef; }

    public PropertyName paramName(@Initialized int i) {
        BeanPropertyDefinition propDef = _params[i].propDef;
        if (propDef != null) {
            return propDef.getFullName();
        }
        return null;
    }

    public PropertyName explicitParamName(@Initialized int i) {
        BeanPropertyDefinition propDef = _params[i].propDef;
        if (propDef != null) {
            if (propDef.isExplicitlyNamed()) {
                return propDef.getFullName();
            }
        }
        return null;
    }
    
    public PropertyName findImplicitParamName(@Initialized int i) {
        String str = _intr.findImplicitPropertyName(_params[i].annotated);
        if (str != null && !str.isEmpty()) {
            return PropertyName.construct(str);
        }
        return null;
    }

    /**
     * Specialized accessor that finds index of the one and only parameter
     * with NO injection and returns that; or, if none or more than one found,
     * returns -1.
     */
    public int findOnlyParamWithoutInjection()
    {
        int missing = -1;
        for (int i = 0; i < _paramCount; ++i) {
            if (_params[i].injection == null) {
                if (missing >= 0) {
                    return -1;
                }
                missing = i;
            }
        }
        return missing;
    }

    @Override
    public String toString(@Initialized CreatorCandidate this) {
        return _creator.toString();
    }

    public final static class Param {
        public final @Initialized AnnotatedParameter annotated;
        public final @Initialized BeanPropertyDefinition propDef;
        public final JacksonInject.@Initialized Value injection;

        public Param(@Initialized AnnotatedParameter p, @Initialized @Nullable BeanPropertyDefinition pd,
                JacksonInject.@Initialized Value i)
        {
            annotated = p;
            propDef = pd;
            injection = i;
        }

        public PropertyName fullName() {
            if (propDef == null) {
                return null;
            }
            return propDef.getFullName();
        }

        public boolean hasFullName() {
            if (propDef == null) {
                return false;
            }
            PropertyName n = propDef.getFullName();
            return n.hasSimpleName();
        }
    }
}
