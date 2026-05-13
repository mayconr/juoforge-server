package com.github.mayconr.juoserver.network.session.i18n;

import com.github.mayconr.juoserver.game.model.event.message.LocalizedMessageContent;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ResourceBundleMessageLocalizer
        implements MessageLocalizer {

    private final String baseName;

    private final ClientLocale fallbackLocale;

    private final ResourceBundle.Control control =
            new Utf8Control();

    public ResourceBundleMessageLocalizer(String baseName) {
        this(baseName, ClientLocale.defaultLocale());
    }

    public ResourceBundleMessageLocalizer(
            String baseName,
            ClientLocale fallbackLocale
    ) {

        if (baseName == null || baseName.isBlank()) {
            throw new IllegalArgumentException(
                    "baseName cannot be null or blank"
            );
        }

        this.baseName = baseName;

        this.fallbackLocale =
                fallbackLocale == null
                        ? ClientLocale.defaultLocale()
                        : fallbackLocale;
    }

    @Override
    public String localize(
            LocalizedMessageContent content,
            ClientLocale locale
    ) {

        if (content == null) {
            throw new IllegalArgumentException(
                    "content cannot be null"
            );
        }

        final ClientLocale effectiveLocale =
                locale == null
                        ? fallbackLocale
                        : locale;

        final String key =
                normalizeKey(content.key());

        if (key == null || key.isBlank()) {
            return "";
        }

        final String pattern =
                resolvePattern(
                        key,
                        effectiveLocale
                );

        return format(
                pattern,
                content.params(),
                effectiveLocale.toJavaLocale()
        );
    }

    private String normalizeKey(String key) {

        if (key == null) {
            return null;
        }

        String normalized = key.trim();

        if (normalized.startsWith("{")
                && normalized.endsWith("}")
                && normalized.length() > 2) {

            normalized = normalized.substring(
                    1,
                    normalized.length() - 1
            );
        }

        return normalized;
    }

    private String resolvePattern(
            String key,
            ClientLocale locale
    ) {

        try {

            ResourceBundle bundle =
                    ResourceBundle.getBundle(
                            baseName,
                            locale.toJavaLocale(),
                            control
                    );

            if (bundle.containsKey(key)) {
                return bundle.getString(key);
            }

        } catch (MissingResourceException ignored) {
        }

        if (!locale.equals(fallbackLocale)) {

            try {

                ResourceBundle fallbackBundle =
                        ResourceBundle.getBundle(
                                baseName,
                                fallbackLocale.toJavaLocale(),
                                control
                        );

                if (fallbackBundle.containsKey(key)) {
                    return fallbackBundle.getString(key);
                }

            } catch (MissingResourceException ignored) {
            }
        }

        return "!" + key + "!";
    }

    private String format(
            String pattern,
            Map<String, Object> params,
            Locale locale
    ) {

        if (pattern == null) {
            return "";
        }

        if (params == null || params.isEmpty()) {
            return pattern;
        }

        String resolved = pattern;

        for (Map.Entry<String, Object> entry : params.entrySet()) {

            resolved = resolved.replace(
                    "{" + entry.getKey() + "}",
                    stringify(entry.getValue())
            );
        }

        return new MessageFormat(
                resolved,
                locale
        ).format(new Object[0]);
    }

    private String stringify(Object value) {
        return value == null
                ? "null"
                : String.valueOf(value);
    }
}
