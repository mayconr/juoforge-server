package com.github.mayconr.juoserver.game.ui.contextmenu;

import com.github.mayconr.juoserver.infrastructure.i18n.ClientLocale;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.SequencedMap;

/**
 * Context menu clilocs by client locale, in the legacy context menu cliloc range.
 *
 * <p>The first entry is the fallback used when the client locale has no cliloc.
 */
public record LocalizedCliloc(SequencedMap<ClientLocale, Integer> clilocs) {
    private static final int MIN_CLILOC = 3000000;
    private static final int MAX_CLILOC = 3065535;

    public LocalizedCliloc {
        Objects.requireNonNull(clilocs, "Context menu clilocs are required");
        if (clilocs.isEmpty()) {
            throw new IllegalArgumentException("At least one context menu cliloc is required");
        }
        clilocs = copyOf(clilocs);
    }

    /** Creates a cliloc used for every client locale. */
    public static LocalizedCliloc of(int cliloc) {
        var clilocs = new LinkedHashMap<ClientLocale, Integer>();
        clilocs.put(ClientLocale.defaultLocale(), cliloc);
        return new LocalizedCliloc(clilocs);
    }

    /** Returns the cliloc for {@code locale}, or the first cliloc when there is no match. */
    public int resolve(ClientLocale locale) {
        var cliloc = locale == null ? null : clilocs.get(locale);
        return cliloc != null ? cliloc : clilocs.firstEntry().getValue();
    }

    private static SequencedMap<ClientLocale, Integer> copyOf(SequencedMap<ClientLocale, Integer> clilocs) {
        var copy = new LinkedHashMap<ClientLocale, Integer>();
        clilocs.forEach((locale, cliloc) -> copy.put(
                Objects.requireNonNull(locale, "Context menu cliloc locale is required"),
                requireLegacyRange(cliloc)));
        return Collections.unmodifiableSequencedMap(copy);
    }

    private static int requireLegacyRange(Integer cliloc) {
        Objects.requireNonNull(cliloc, "Context menu cliloc is required");
        if (cliloc < MIN_CLILOC || cliloc > MAX_CLILOC) {
            throw new IllegalArgumentException("Context menu cliloc is outside the legacy range");
        }
        return cliloc;
    }
}
