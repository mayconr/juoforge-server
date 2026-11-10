package com.github.mayconr.juoserver.game.ui.gump;

/** A decorative divider drawn by repeating a gump image within the reserved layout area. */
public final class Separator implements UIElement, Sized {
    private boolean fill;
    private int layoutWidth, layoutHeight;
    private final int gumpId;
    private final int width;
    private final int height;
    private int x;
    private int y;

    public Separator(int gumpId, int width, int height) {
        if (gumpId < 0 || gumpId > 0xFFFF) throw new IllegalArgumentException("Invalid gump ID");
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Separator dimensions must be positive");
        this.gumpId = gumpId;
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
    public void render(GumpBuilder builder) {
        builder.gumpPicTiled(x, y, getWidth(), getHeight(), gumpId);
    }

    @Override
    public int getWidth() { return fill ? layoutWidth : width; }

    @Override
    public int getHeight() { return fill ? layoutHeight : height; }
    @Override
    public int getPreferredWidth() { return width; }

    @Override
    public int getPreferredHeight() { return height; }
}
