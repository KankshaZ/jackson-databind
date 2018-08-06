package com.fasterxml.jackson.databind.deser.std;

import org.checkerframework.checker.initialization.qual.Initialized;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.Timestamp;
import java.text.*;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.StdDateFormat;

/**
 * Container class for core JDK date/time type deserializers.
 */
@SuppressWarnings("serial")
public class DateDeserializers
{
    private final static @Initialized HashSet<String> _classNames = new HashSet<String>();
    static {
        Class<?>[] numberTypes = new Class<?>[] {
            Calendar.class,
            GregorianCalendar.class,
            java.sql.Date.class,
            java.util.Date.class,
            Timestamp.class,
        };
        for (Class<?> cls : numberTypes) {
            _classNames.add(cls.getName());
        }
    }

    public static JsonDeserializer<?> find(@Initialized Class<?> rawType, @Initialized String clsName)
    {
        if (_classNames.contains(clsName)) {
            // Start with the most common type
            if (rawType == Calendar.class) {
                return new CalendarDeserializer();
            }
            if (rawType == java.util.Date.class) {
                return DateDeserializer.instance;
            }
            if (rawType == java.sql.Date.class) {
                return new SqlDateDeserializer();
            }
            if (rawType == Timestamp.class) {
                return new TimestampDeserializer();
            }
            if (rawType == GregorianCalendar.class) {
                return new CalendarDeserializer(GregorianCalendar.class);
            }
        }
        return null;
    }

    /*
    /**********************************************************
    /* Intermediate class for Date-based ones
    /**********************************************************
     */

    protected abstract static class DateBasedDeserializer<T>
        extends StdScalarDeserializer<T>
        implements ContextualDeserializer
    {
        /**
         * Specific format to use, if non-null; if null will
         * just use default format.
         */
        protected final @Initialized DateFormat _customFormat;

        /**
         * Let's also keep format String for reference, to use for error messages
         */
        protected final @Initialized String _formatString;

        protected DateBasedDeserializer(Class<?> clz) {
            super(clz);
            _customFormat = null;
            _formatString = null;
        }

        protected DateBasedDeserializer(DateBasedDeserializer<T> base,
                DateFormat format, String formatStr) {
            super(base._valueClass);
            _customFormat = format;
            _formatString = formatStr;
        }

        protected abstract DateBasedDeserializer<T> withDateFormat(@Initialized DateFormat df, @Initialized String formatStr);

        @Override
        public JsonDeserializer<?> createContextual(DateDeserializers.@Initialized @Initialized DateBasedDeserializer<T> this, @Initialized DeserializationContext ctxt,
                @Initialized
                BeanProperty property)
           throws JsonMappingException
        {
            final JsonFormat.Value format = findFormatOverrides(ctxt, property,
                    handledType());

            if (format != null) {
                TimeZone tz = format.getTimeZone();
                final Boolean lenient = format.getLenient();

                // First: fully custom pattern?
                if (format.hasPattern()) {
                    final String pattern = format.getPattern();
                    final Locale loc = format.hasLocale() ? format.getLocale() : ctxt.getLocale();
                    SimpleDateFormat df = new SimpleDateFormat(pattern, loc);
                    if (tz == null) {
                        tz = ctxt.getTimeZone();
                    }
                    df.setTimeZone(tz);
                    if (lenient != null) {
                        df.setLenient(lenient);
                    }
                    return withDateFormat(df, pattern);
                }
                // But if not, can still override timezone
                if (tz != null) {
                    DateFormat df = ctxt.getConfig().getDateFormat();
                    // one shortcut: with our custom format, can simplify handling a bit
                    if (df.getClass() == StdDateFormat.class) {
                        final Locale loc = format.hasLocale() ? format.getLocale() : ctxt.getLocale();
                        StdDateFormat std = (StdDateFormat) df;
                        std = std.withTimeZone(tz);
                        std = std.withLocale(loc);
                        if (lenient != null) {
                            std = std.withLenient(lenient);
                        }
                        df = std;
                    } else {
                        // otherwise need to clone, re-set timezone:
                        df = (DateFormat) df.clone();
                        df.setTimeZone(tz);
                        if (lenient != null) {
                            df.setLenient(lenient);
                        }
                    }
                    return withDateFormat(df, _formatString);
                }
                // or maybe even just leniency?
                if (lenient != null) {
                    DateFormat df = ctxt.getConfig().getDateFormat();
                    String pattern = _formatString;
                    // one shortcut: with our custom format, can simplify handling a bit
                    if (df.getClass() == StdDateFormat.class) {
                        StdDateFormat std = (StdDateFormat) df;
                        std = std.withLenient(lenient);
                        df = std;
                        pattern = std.toPattern();
                    } else {
                        // otherwise need to clone,
                        df = (DateFormat) df.clone();
                        df.setLenient(lenient);
                        if (df instanceof SimpleDateFormat) {
                            ((SimpleDateFormat) df).toPattern();
                        }
                    }
                    if (pattern == null) {
                        pattern = "[unknown]";
                    }
                    return withDateFormat(df, pattern);
                }
            }
            return this;
        }

        @Override
        protected java.util.Date _parseDate(DateDeserializers.@Initialized @Initialized DateBasedDeserializer<T> this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt)
            throws IOException
        {
            if (_customFormat != null) {
                if (p.hasToken(JsonToken.VALUE_STRING)) {
                    String str = p.getText().trim();
                    if (str.length() == 0) {
                        return (Date) getEmptyValue(ctxt);
                    }
                    synchronized (_customFormat) {
                        try {
                            return _customFormat.parse(str);
                        } catch (ParseException e) {
                            return (java.util.Date) ctxt.handleWeirdStringValue(handledType(), str,
                                    "expected format \"%s\"", _formatString);
                        }
                    }
                }
            }
            return super._parseDate(p, ctxt);
        }
    }

    /*
    /**********************************************************
    /* Deserializer implementations for Date types
    /**********************************************************
     */

    @JacksonStdImpl
    public static class CalendarDeserializer extends DateBasedDeserializer<Calendar>
    {
        /**
         * We may know actual expected type; if so, it will be
         * used for instantiation.
         *
         * @since 2.9
         */
        protected final @Initialized Constructor<Calendar> _defaultCtor;

        public CalendarDeserializer() {
            super(Calendar.class);
            _defaultCtor = null;
        }

        @SuppressWarnings("unchecked")
        public CalendarDeserializer(@Initialized Class<? extends Calendar> cc) {
            super(cc);
            _defaultCtor = (Constructor<Calendar>) ClassUtil.findConstructor(cc, false);
        }

        public CalendarDeserializer(@Initialized CalendarDeserializer src, @Initialized DateFormat df, @Initialized String formatString) {
            super(src, df, formatString);
            _defaultCtor = src._defaultCtor;
        }

        @Override
        protected CalendarDeserializer withDateFormat(DateDeserializers.@Initialized CalendarDeserializer this, @Initialized DateFormat df, @Initialized String formatString) {
            return new CalendarDeserializer(this, df, formatString);
        }

        @Override
        public Calendar deserialize(DateDeserializers.@Initialized CalendarDeserializer this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt) throws IOException
        {
            Date d = _parseDate(p, ctxt);
            if (d == null) {
                return null;
            }
            if (_defaultCtor == null) {
                return ctxt.constructCalendar(d);
            }
            try {
                Calendar c = _defaultCtor.newInstance();            
                c.setTimeInMillis(d.getTime());
                TimeZone tz = ctxt.getTimeZone();
                if (tz != null) {
                    c.setTimeZone(tz);
                }
                return c;
            } catch (Exception e) {
                return (Calendar) ctxt.handleInstantiationProblem(handledType(), d, e);
            }
        }
    }

    /**
     * Simple deserializer for handling {@link java.util.Date} values.
     *<p>
     * One way to customize Date formats accepted is to override method
     * {@link DeserializationContext#parseDate} that this basic
     * deserializer calls.
     */
    @JacksonStdImpl
    public static class DateDeserializer extends DateBasedDeserializer<Date>
    {
        public final static @Initialized DateDeserializer instance = new DateDeserializer();

        public DateDeserializer() { super(Date.class); }
        public DateDeserializer(@Initialized DateDeserializer base, @Initialized DateFormat df, @Initialized String formatString) {
            super(base, df, formatString);
        }

        @Override
        protected DateDeserializer withDateFormat(DateDeserializers.@Initialized DateDeserializer this, @Initialized DateFormat df, @Initialized String formatString) {
            return new DateDeserializer(this, df, formatString);
        }
        
        @Override
        public java.util.Date deserialize(DateDeserializers.@Initialized DateDeserializer this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt) throws IOException {
            return _parseDate(p, ctxt);
        }
    }

    /**
     * Compared to plain old {@link java.util.Date}, SQL version is easier
     * to deal with: mostly because it is more limited.
     */
    public static class SqlDateDeserializer
        extends DateBasedDeserializer<java.sql.Date>
    {
        public SqlDateDeserializer() { super(java.sql.Date.class); }
        public SqlDateDeserializer(@Initialized SqlDateDeserializer src, @Initialized DateFormat df, @Initialized String formatString) {
            super(src, df, formatString);
        }

        @Override
        protected SqlDateDeserializer withDateFormat(DateDeserializers.@Initialized SqlDateDeserializer this, @Initialized DateFormat df, @Initialized String formatString) {
            return new SqlDateDeserializer(this, df, formatString);
        }
        
        @Override
        public java.sql.Date deserialize(DateDeserializers.@Initialized SqlDateDeserializer this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt) throws IOException {
            Date d = _parseDate(p, ctxt);
            return (d == null) ? null : new java.sql.Date(d.getTime());
        }
    }

    /**
     * Simple deserializer for handling {@link java.sql.Timestamp} values.
     *<p>
     * One way to customize Timestamp formats accepted is to override method
     * {@link DeserializationContext#parseDate} that this basic
     * deserializer calls.
     */
    public static class TimestampDeserializer extends DateBasedDeserializer<Timestamp>
    {
        public TimestampDeserializer() { super(Timestamp.class); }
        public TimestampDeserializer(@Initialized TimestampDeserializer src, @Initialized DateFormat df, @Initialized String formatString) {
            super(src, df, formatString);
        }

        @Override
        protected TimestampDeserializer withDateFormat(DateDeserializers.@Initialized TimestampDeserializer this, @Initialized DateFormat df, @Initialized String formatString) {
            return new TimestampDeserializer(this, df, formatString);
        }
        
        @Override
        public java.sql.Timestamp deserialize(DateDeserializers.@Initialized TimestampDeserializer this, @Initialized JsonParser p, @Initialized DeserializationContext ctxt) throws IOException
        {
            Date d = _parseDate(p, ctxt);
            return (d == null) ? null : new Timestamp(d.getTime());
        }
    }
}
