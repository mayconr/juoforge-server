package com.github.mayconr.juoserver.game.world.module.ui.gump;

public interface UIElement {
    void layout(LayoutContext ctx);

    void render(GumpBuilder g);
}
