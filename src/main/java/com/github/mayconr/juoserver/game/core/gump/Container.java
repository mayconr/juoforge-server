package com.github.mayconr.juoserver.game.core.gump;

import java.util.ArrayList;
import java.util.List;

abstract class Container implements UIElement {
    protected final List<UIElement> children = new ArrayList<>();
    protected int x, y, width, height;

    Container(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Container add(UIElement e) {
        children.add(e);
        return this;
    }

    @Override
    public void render(GumpBuilder g) {
        for (UIElement e : children) e.render(g);
    }
}
