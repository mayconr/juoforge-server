package com.github.mayconr.juoserver.game.ui.gump;

public class TextArea extends TooltipElement implements Sized {
    private boolean fill;
    private int layoutWidth, layoutHeight;

    private final int entryId;
    private final int width;
    private final int height;
    private int x, y;

    TextArea(int entryId, int width, int height) {
        this.entryId = entryId;
        this.width = width;
        this.height = height;
    }

    @Override
    public void layout(LayoutContext ctx) {
        fill = ctx.fill;
        layoutWidth = Math.max(0, ctx.width);
        layoutHeight = Math.max(0, ctx.height);
        x = ctx.x;
        y = ctx.y;
    }

    @Override
    public void render(GumpBuilder g) {
        g.resizePic(x, y, 3000, getWidth(), getHeight());
        renderTooltip(g);
        g.textEntry(x + 4, y + 4, getWidth() - 8, getHeight() - 8, 0, entryId, -1);
        renderTooltip(g);
    }

    @Override
    public int getWidth() {
        return fill ? layoutWidth : width;
    }

    @Override
    public int getHeight() {
        return fill ? layoutHeight : height;
    }

    @Override
    public int getPreferredWidth() { return width; }

    @Override
    public int getPreferredHeight() { return height; }
}
