package com.github.mayconr.juoserver.game.ui.contextmenu;

import com.github.mayconr.juoserver.infrastructure.i18n.ClientLocale;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A registered menu entry, using the legacy context menu cliloc range.
 *
 * <p>The cliloc may vary by client locale; the first cliloc is sent when the client
 * locale has no match. Modules may extend this class to declare each entry in its own type.
 */
public class ContextMenuEntry {
    private static final Consumer<ContextMenuContext> NO_ACTION = context -> { };

    private final LocalizedCliloc cliloc;
    private final Predicate<ContextMenuContext> predicate;
    private final Consumer<ContextMenuContext> callback;

    /** Creates a display-only entry without a selection action. */
    public ContextMenuEntry(int cliloc, Predicate<ContextMenuContext> predicate) {
        this(LocalizedCliloc.of(cliloc), predicate, NO_ACTION);
    }

    public ContextMenuEntry(
            int cliloc,
            Predicate<ContextMenuContext> predicate,
            Consumer<ContextMenuContext> callback) {
        this(LocalizedCliloc.of(cliloc), predicate, callback);
    }

    /** Creates a display-only entry with a cliloc per client locale. */
    public ContextMenuEntry(
            SequencedMap<ClientLocale, Integer> clilocs,
            Predicate<ContextMenuContext> predicate) {
        this(new LocalizedCliloc(clilocs), predicate, NO_ACTION);
    }

    /** Creates an entry with a cliloc per client locale; the first cliloc is the fallback. */
    public ContextMenuEntry(
            SequencedMap<ClientLocale, Integer> clilocs,
            Predicate<ContextMenuContext> predicate,
            Consumer<ContextMenuContext> callback) {
        this(new LocalizedCliloc(clilocs), predicate, callback);
    }

    private ContextMenuEntry(
            LocalizedCliloc cliloc,
            Predicate<ContextMenuContext> predicate,
            Consumer<ContextMenuContext> callback) {
        this.cliloc = cliloc;
        this.predicate = Objects.requireNonNull(predicate, "Context menu predicate is required");
        this.callback = Objects.requireNonNull(callback, "Context menu callback is required");
    }

    public final LocalizedCliloc cliloc() {
        return cliloc;
    }

    public final Predicate<ContextMenuContext> predicate() {
        return predicate;
    }

    public final Consumer<ContextMenuContext> callback() {
        return callback;
    }
}
