package com.github.mayconr.juoserver.game.ui.gump;

public interface UIElement {
    void layout(LayoutContext ctx);

    void render(GumpBuilder g);
}
