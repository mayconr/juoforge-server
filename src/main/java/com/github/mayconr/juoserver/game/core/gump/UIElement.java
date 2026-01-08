package com.github.mayconr.juoserver.game.core.gump;

public interface UIElement {
    void layout(LayoutContext ctx);

    void render(GumpBuilder g);
}
