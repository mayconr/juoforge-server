package com.github.mayconr.juoserver.game.ui.gump;

import java.util.ArrayList;
import java.util.List;

public class Page implements UIElement {
    private final int index;
    private final List<UIElement> children = new ArrayList<>();

    public Page(int index) {
        if (index < 0) throw new IllegalArgumentException("page must be >= 0");
        this.index = index;
    }

    public Page add(UIElement e) {
        children.add(e);
        return this;
    }

    @Override
    public void layout(LayoutContext ctx) {
        for (UIElement e : children) e.layout(ctx);
    }

    @Override
    public void render(GumpBuilder g) {
        g.page(index);
        for (UIElement e : children) e.render(g);
    }

    public int getIndex() {
        return index;
    }
}
