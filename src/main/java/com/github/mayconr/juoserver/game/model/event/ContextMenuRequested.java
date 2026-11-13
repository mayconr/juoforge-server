package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.ui.contextmenu.LocalizedCliloc;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import java.util.List;

/** A resolved item or mobile menu request, containing only entries allowed by their predicates. */
public record ContextMenuRequested(UOPlayer player, int serialId, List<Entry> entries)
        implements GameEvent {
    public ContextMenuRequested {
        entries = List.copyOf(entries);
    }

    /**
     * A rendered option with a selection identifier assigned by the UI module.
     * The cliloc is resolved with the receiving client locale when sent.
     */
    public record Entry(int index, LocalizedCliloc cliloc) { }
}
