package com.github.mayconr.juoserver.game.ui.contextmenu;

import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.game.model.UOPlayer;

/** The requesting player and the resolved item or mobile target. */
public sealed interface ContextMenuContext
        permits ItemContextMenuContext, MobileContextMenuContext {

    UOPlayer player();

    UOObject<?> target();
}
