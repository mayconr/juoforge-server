package com.github.mayconr.juoserver.game.ui.gump;

/** A decorative divider drawn by repeating a gump image within the reserved layout area. */
public final class Separator implements UIElement, Sized {
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
        x = ctx.x;
        y = ctx.y;
    }

    @Override
    public void render(GumpBuilder builder) {
        builder.gumpPicTiled(x, y, width, height, gumpId);
    }

    @Override
    public int getWidth() { return width; }

    @Override
    public int getHeight() { return height; }
}
