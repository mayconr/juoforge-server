package com.github.mayconr.juoserver.game.ui.gump;

final public class LayoutContext {
    int x, y, width, height;
    boolean fill;

    LayoutContext(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public LayoutContext child(int x, int y, int width, int height) {
        return new LayoutContext(x, y, width, height);
    }
}
