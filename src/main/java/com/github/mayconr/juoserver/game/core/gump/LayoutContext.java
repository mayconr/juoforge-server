package com.github.mayconr.juoserver.game.core.gump;

final class LayoutContext {
    int x, y, width, height;

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
