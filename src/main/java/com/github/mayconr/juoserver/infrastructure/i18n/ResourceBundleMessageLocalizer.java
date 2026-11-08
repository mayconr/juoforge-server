package com.github.mayconr.juoserver.infrastructure.i18n;

import com.github.mayconr.juoserver.game.model.event.message.LocalizedMessageContent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Resolves each locale across all bundles before considering fallback locales or root bundles. */
public class ResourceBundleMessageLocalizer implements MessageLocalizer {
    private final List<String> baseNames;
    private final ClientLocale fallbackLocale;
    private final ClassLoader loader;
    private final ResourceBundle.Control control = new Utf8Control();
    private final Map<BundleKey, Optional<ResourceBundle>> bundles = new ConcurrentHashMap<>();

    private record BundleKey(String baseName, Locale locale) {}

    public ResourceBundleMessageLocalizer(String baseName) {
        this(baseName, ClientLocale.defaultLocale());
    }

    public ResourceBundleMessageLocalizer(String baseName, ClientLocale fallbackLocale) {
        this(Collections.singletonList(baseName), fallbackLocale);
    }

    /** Bundle order determines precedence for the same locale. */
    public ResourceBundleMessageLocalizer(List<String> baseNames, ClientLocale fallbackLocale) {
        if (baseNames == null || baseNames.isEmpty()
                || baseNames.stream().anyMatch(name -> name == null || name.isBlank())) {
            throw new IllegalArgumentException("Bundle names cannot be null or blank");
        }
        this.baseNames = baseNames.stream().distinct().toList();
        this.fallbackLocale = fallbackLocale == null ? ClientLocale.defaultLocale() : fallbackLocale;
        var contextLoader = Thread.currentThread().getContextClassLoader();
        this.loader = contextLoader == null ? getClass().getClassLoader() : contextLoader;
    }

    @Override
    public String localize(LocalizedMessageContent content, ClientLocale locale) {
        if (content == null) throw new IllegalArgumentException("content cannot be null");
        var effectiveLocale = locale == null ? fallbackLocale : locale;
        var key = normalizeKey(content.key());
        if (key == null || key.isBlank()) return "";
        return format(resolvePattern(key, effectiveLocale), content.params(), effectiveLocale.toJavaLocale());
    }

    private String normalizeKey(String key) {
        if (key == null) return null;
        var normalized = key.trim();
        if (normalized.startsWith("{") && normalized.endsWith("}") && normalized.length() > 2) {
            return normalized.substring(1, normalized.length() - 1);
        }
        return normalized;
    }

    private String resolvePattern(String key, ClientLocale locale) {
        var locales = new LinkedHashSet<Locale>();
        addCandidates(locales, locale.toJavaLocale());
        addCandidates(locales, fallbackLocale.toJavaLocale());
        locales.add(Locale.ROOT);
        for (var candidate : locales) {
            for (var baseName : baseNames) {
                var bundle = bundles.computeIfAbsent(new BundleKey(baseName, candidate), this::loadBundle);
                if (bundle.isPresent() && bundle.get().containsKey(key)) {
                    return bundle.get().getString(key);
                }
            }
        }
        return "!" + key + "!";
    }

    private void addCandidates(Set<Locale> locales, Locale locale) {
        for (var candidate : control.getCandidateLocales(baseNames.getFirst(), locale)) {
            if (!candidate.equals(Locale.ROOT)) locales.add(candidate);
        }
    }

    private Optional<ResourceBundle> loadBundle(BundleKey key) {
        try {
            // Load exact locales so a bundle's root cannot hide another bundle's translation.
            return Optional.ofNullable(control.newBundle(key.baseName(), key.locale(), "java.properties", loader, false));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot load message bundle " + key, e);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException("Cannot load message bundle " + key, e);
        }
    }

    private String format(String pattern, Map<String, Object> params, Locale locale) {
        if (params == null || params.isEmpty()) return pattern;
        var resolved = pattern;
        for (var entry : params.entrySet()) {
            resolved = resolved.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return new MessageFormat(resolved, locale).format(new Object[0]);
    }
}
