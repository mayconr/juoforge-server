package com.github.mayconr.juoserver.game.gump;

import java.util.List;

public class Row implements UIElement {

    private final List<UIElement> children;
    private final int gap;

    public Row(int gap, List<UIElement> children) {
        this.gap = gap;
        this.children = children;
    }

    @Override
    public void layout(LayoutContext ctx) {

        int y = ctx.y;

        for (UIElement child : children) {

            int height = 0;

            if (child instanceof Sized s) {
                height = s.getHeight();
            } else {
                height = 48; // altura padrão de campo
            }

            LayoutContext childCtx = new LayoutContext(ctx.x, y, ctx.width, height);

            child.layout(childCtx);
            y += height + gap;
        }
    }

    @Override
    public void render(GumpBuilder g) {
        for (UIElement child : children) {
            child.render(g);
        }
    }
}
