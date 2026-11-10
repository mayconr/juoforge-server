package com.github.mayconr.juoserver.game.ui.gump;

/** Client virtue artwork with its built-in tooltip and VirtueSelected double-click response. */
public final class VirtueImage extends TooltipElement implements Sized {
    private final int graphicId;
    private final int width;
    private final int height;
    private int hue;
    private int x, y;

    public VirtueImage(int graphicId) {
        this(graphicId, 50, 50);
    }

    /** Dimensions reserve layout space; client artwork is not resized. */
    public VirtueImage(int graphicId, int width, int height) {
        if (graphicId < 0x69 || graphicId > 0x70) {
            throw new IllegalArgumentException("Virtue graphic must be between 0x69 and 0x70");
        }
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Virtue dimensions must be positive");
        this.graphicId = graphicId;
        this.width = width;
        this.height = height;
    }

    /** Sets the encoded gump hue used by the client to choose a virtue tier tooltip. */
    public VirtueImage hue(int hue) {
        if (hue < 0 || hue > 0xFFFF) throw new IllegalArgumentException("Hue must fit an unsigned short");
        this.hue = hue;
        return this;
    }

    @Override
    public void layout(LayoutContext ctx) {
        x = ctx.x;
        y = ctx.y;
    }

    @Override
    public void render(GumpBuilder builder) {
        builder.virtueGumpItem(x, y, graphicId, hue);
        renderTooltip(builder);
    }

    @Override
    public int getWidth() { return width; }

    @Override
    public int getHeight() { return height; }
}
