package com.github.mayconr.juoserver.game.ui.contextmenu;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** A registered menu entry, using the legacy context menu cliloc range. */
public record ContextMenuEntry(
        int cliloc,
        Predicate<ContextMenuContext> predicate,
        Consumer<ContextMenuContext> callback) {

    /** Creates a display-only entry without a selection action. */
    public ContextMenuEntry(int cliloc, Predicate<ContextMenuContext> predicate) {
        this(cliloc, predicate, context -> { });
    }

    public ContextMenuEntry {
        if (cliloc < 3000000 || cliloc > 3065535) {
            throw new IllegalArgumentException("Context menu cliloc is outside the legacy range");
        }
        Objects.requireNonNull(predicate, "Context menu predicate is required");
        Objects.requireNonNull(callback, "Context menu callback is required");
    }
}
