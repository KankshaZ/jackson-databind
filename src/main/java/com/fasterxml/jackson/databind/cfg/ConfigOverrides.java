package com.fasterxml.jackson.databind.cfg;

import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

/**
 * Container for individual {@link ConfigOverride} values.
 * 
 * @since 2.8
 */
public class ConfigOverrides
    implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Per-type override definitions
     */
    protected @Nullable Map<Class<?>, MutableConfigOverride> _overrides;

    // // // Global defaulting

    /**
     * @since 2.9
     */
    protected JsonInclude.@Initialized Value _defaultInclusion;

    /**
     * @since 2.9
     */
    protected JsonSetter.@Initialized Value _defaultSetterInfo;

    /**
     * @since 2.9
     */
    protected VisibilityChecker<?> _visibilityChecker;

    /**
     * @since 2.9
     */
    protected @Nullable Boolean _defaultMergeable;

    /*
    /**********************************************************
    /* Life cycle
    /**********************************************************
     */

    public ConfigOverrides() {
        this(null,
                // !!! TODO: change to (ALWAYS, ALWAYS)?
                JsonInclude.Value.empty(),
                JsonSetter.Value.empty(),
                VisibilityChecker.Std.defaultInstance(),
                null
        );
    }

    protected ConfigOverrides(@Initialized @Nullable Map<Class<?>, MutableConfigOverride> overrides,
            JsonInclude.@Initialized Value defIncl,
            JsonSetter.@Initialized Value defSetter,
            @Initialized
            VisibilityChecker<?> defVisibility,
            @Initialized
            @Nullable
            Boolean defMergeable) {
        _overrides = overrides;
        _defaultInclusion = defIncl;
        _defaultSetterInfo = defSetter;
        _visibilityChecker = defVisibility;
        _defaultMergeable = defMergeable;
    }

    @SuppressWarnings("nullness") // _overrides.entrySet() only called if _overrides is non-null
    public ConfigOverrides copy()
    {
        Map<Class<?>, MutableConfigOverride> newOverrides;
        if (_overrides == null) {
            newOverrides = null;
        } else {
            newOverrides = _newMap();
            for (Map.Entry<Class<?>, MutableConfigOverride> entry : _overrides.entrySet()) {
                newOverrides.put(entry.getKey(), entry.getValue().copy());
            }
        }
        return new ConfigOverrides(newOverrides,
                _defaultInclusion, _defaultSetterInfo, _visibilityChecker, _defaultMergeable);
    }

    /*
    /**********************************************************
    /* Per-type override access
    /**********************************************************
     */
    
    public @Nullable ConfigOverride findOverride(@Initialized Class<?> type) {
        if (_overrides == null) {
            return null;
        }
        return _overrides.get(type);
    }

    public MutableConfigOverride findOrCreateOverride(Class<?> type) {
        if (_overrides == null) {
            _overrides = _newMap();
        }
        MutableConfigOverride override = _overrides.get(type);
        if (override == null) {
            override = new MutableConfigOverride();
            _overrides.put(type, override);
        }
        return override;
    }

    /*
    /**********************************************************
    /* Global defaults access
    /**********************************************************
     */

    public JsonInclude.Value getDefaultInclusion() {
        return _defaultInclusion;
    }

    public JsonSetter.Value getDefaultSetterInfo() {
        return _defaultSetterInfo;
    }

    public @Nullable Boolean getDefaultMergeable() {
        return _defaultMergeable;
    }

    /**
     * @since 2.9
     */
    public VisibilityChecker<?> getDefaultVisibility() {
        return _visibilityChecker;
    }

    /**
     * @since 2.9
     */
    public void setDefaultInclusion(JsonInclude.Value v) {
        _defaultInclusion = v;
    }

    /**
     * @since 2.9
     */
    public void setDefaultSetterInfo(JsonSetter.Value v) {
        _defaultSetterInfo = v;
    }

    /**
     * @since 2.9
     */
    public void setDefaultMergeable(Boolean v) {
        _defaultMergeable = v;
    }

    /**
     * @since 2.9
     */
    public void setDefaultVisibility(VisibilityChecker<?> v) {
        _visibilityChecker = v;
    }

    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */
    
    protected Map<Class<?>, MutableConfigOverride> _newMap() {
        return new HashMap<Class<?>, MutableConfigOverride>();
    }
}
