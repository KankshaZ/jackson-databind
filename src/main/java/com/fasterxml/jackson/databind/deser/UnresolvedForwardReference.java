package com.fasterxml.jackson.databind.deser;

import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;

/**
 * Exception thrown during deserialization when there are object id that can't
 * be resolved.
 * 
 * @author pgelinas
 */
public class UnresolvedForwardReference extends JsonMappingException {
    private static final long serialVersionUID = 1L;
    private @Nullable ReadableObjectId _roid;
    private @Nullable List<UnresolvedId> _unresolvedIds;

    /**
     * @since 2.7
     */
    public UnresolvedForwardReference(@Initialized JsonParser p, @Initialized String msg, @Initialized JsonLocation loc, @Initialized ReadableObjectId roid) {
        super(p, msg, loc);
        _roid = roid;
    }

    /**
     * @since 2.7
     */
    public UnresolvedForwardReference(@Initialized JsonParser p, @Initialized String msg) {
        super(p, msg);
        _unresolvedIds = new ArrayList<UnresolvedId>();
    }

    /**
     * @deprecated Since 2.7
     */
    @Deprecated // since 2.7
    public UnresolvedForwardReference(String msg, JsonLocation loc, ReadableObjectId roid) {
        super(msg, loc);
        _roid = roid;
    }

    /**
     * @deprecated Since 2.7
     */
    @Deprecated // since 2.7
    public UnresolvedForwardReference(String msg) {
        super(msg);
        _unresolvedIds = new ArrayList<UnresolvedId>();
    }

    /*
    /**********************************************************
    /* Accessor methods
    /**********************************************************
     */

    public @Nullable ReadableObjectId getRoid() {
        return _roid;
    }

    public Object getUnresolvedId() {
        return _roid.getKey().key;
    }

    public void addUnresolvedId(@Initialized Object id, @Initialized Class<?> type, @Initialized JsonLocation where) {
        _unresolvedIds.add(new UnresolvedId(id, type, where));
    }

    public @Nullable List<UnresolvedId> getUnresolvedIds(){
        return _unresolvedIds;
    }
    
    @Override
    public String getMessage(@Initialized UnresolvedForwardReference this)
    {
        String msg = super.getMessage();
        if (_unresolvedIds == null) {
            return msg;
        }

        StringBuilder sb = new StringBuilder(msg);
        Iterator<UnresolvedId> iterator = _unresolvedIds.iterator();
        while (iterator.hasNext()) {
            UnresolvedId unresolvedId = iterator.next();
            sb.append(unresolvedId.toString());
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append('.');
        return sb.toString();
    }
}
