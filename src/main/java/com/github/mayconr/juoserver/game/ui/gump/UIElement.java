package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.function.Function;

public interface UIElement {
    /** Resolves text before any dimensions or positions are computed. */
    default void resolveTexts(Function<MessageContent, String> resolver) {}

    void layout(LayoutContext ctx);

    void render(GumpBuilder g);
}
