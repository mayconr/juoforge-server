package com.github.mayconr.juoserver.game.gump;

public interface UIElement {
    void layout(LayoutContext ctx);

    void render(GumpBuilder g);
}
