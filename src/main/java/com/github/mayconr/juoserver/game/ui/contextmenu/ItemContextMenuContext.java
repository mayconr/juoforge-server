package com.github.mayconr.juoserver.game.ui.contextmenu;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import java.util.Objects;

/** Context menu state for a resolved item target. */
public record ItemContextMenuContext(UOPlayer player, UOItem target) implements ContextMenuContext {
    public ItemContextMenuContext {
        Objects.requireNonNull(player, "Requesting player is required");
        Objects.requireNonNull(target, "Context menu target is required");
    }
}
