package com.github.mayconr.juoserver.network.session.i18n;

import com.github.mayconr.juoserver.game.model.event.message.LocalizedMessageContent;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ResourceBundleMessageLocalizer implements MessageLocalizer {

    private final String baseName;
    private final ClientLocale fallbackLocale;

    public ResourceBundleMessageLocalizer(String baseName) {
        this(baseName, ClientLocale.defaultLocale());
    }

    public ResourceBundleMessageLocalizer(String baseName, ClientLocale fallbackLocale) {
        if (baseName == null || baseName.isBlank()) {
            throw new IllegalArgumentException("baseName cannot be null or blank");
        }
        this.baseName = baseName;
        this.fallbackLocale = fallbackLocale == null ? ClientLocale.defaultLocale() : fallbackLocale;
    }

    @Override
    public String localize(LocalizedMessageContent content, ClientLocale locale) {
        if (content == null) {
            throw new IllegalArgumentException("content cannot be null");
        }

        final var effectiveLocale = locale == null ? fallbackLocale : locale;

        String pattern = resolvePattern(content, effectiveLocale);
        return format(pattern, content.params(), effectiveLocale.toJavaLocale());
    }

    private String resolvePattern(LocalizedMessageContent content, ClientLocale locale) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale.toJavaLocale());
            if (bundle.containsKey(content.key())) {
                return bundle.getString(content.key());
            }
        } catch (MissingResourceException ignored) {
            // fallback below
        }

        try {
            ResourceBundle fallbackBundle = ResourceBundle.getBundle(baseName, fallbackLocale.toJavaLocale());
            if (fallbackBundle.containsKey(content.key())) {
                return fallbackBundle.getString(content.key());
            }
        } catch (MissingResourceException ignored) {
            // fallback below
        }

        if (content.fallback() != null && !content.fallback().isBlank()) {
            return content.fallback();
        }

        return content.key();
    }

    private String format(String pattern, Map<String, Object> params, Locale locale) {
        if (params == null || params.isEmpty()) {
            return pattern;
        }

        String resolved = pattern;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            resolved = resolved.replace("{" + entry.getKey() + "}", stringify(entry.getValue()));
        }

        return new MessageFormat(resolved, locale).format(new Object[0]);
    }

    private String stringify(Object value) {
        return value == null ? "null" : String.valueOf(value);
    }
}
