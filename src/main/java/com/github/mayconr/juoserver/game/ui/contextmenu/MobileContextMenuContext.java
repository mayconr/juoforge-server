package com.github.mayconr.juoserver.game.ui.contextmenu;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import java.util.Objects;

/** Context menu state for a resolved mobile target. */
public record MobileContextMenuContext(UOPlayer player, UOMobile target) implements ContextMenuContext {
    public MobileContextMenuContext {
        Objects.requireNonNull(player, "Requesting player is required");
        Objects.requireNonNull(target, "Context menu target is required");
    }
}
