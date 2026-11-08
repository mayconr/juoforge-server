package com.github.mayconr.juoserver.game.ui.gump;

public class ItemIcon extends TooltipElement implements Sized {
    private int x, y;
    private final int tileId;
    private final int width;
    private final int height;
    private boolean centered;
    private int offsetX;
    private int offsetY;
    private int hue;
    private int visibleX;
    private int visibleY;
    private int visibleWidth;
    private int visibleHeight;

    public ItemIcon(int tileId) {
        this(tileId, 30, 30);
    }

    /** Actual art dimensions used for layout only; tilepic does not resize client artwork. */
    public ItemIcon(int tileId, int width, int height) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Invalid item art dimensions");
        this.tileId = tileId;
        this.width = width;
        this.height = height;
        visibleWidth = width;
        visibleHeight = height;
    }

    /** Uses nontransparent art bounds for alignment; the client still draws the original tile. */
    public ItemIcon visibleBounds(int x, int y, int width, int height) {
        if (x < 0 || y < 0 || width <= 0 || height <= 0
                || (long) x + width > this.width || (long) y + height > this.height)
            throw new IllegalArgumentException("Visible bounds must fit within the item art");
        visibleX = x;
        visibleY = y;
        visibleWidth = width;
        visibleHeight = height;
        return this;
    }

    public ItemIcon centered() {
        centered = true;
        return this;
    }

    /** Sets the UO hue; zero preserves the original artwork colors. */
    public ItemIcon hue(int hue) {
        if (hue < 0 || hue > 0xFFFF) throw new IllegalArgumentException("Hue must fit an unsigned short");
        this.hue = hue;
        return this;
    }

    /** Applies a visual adjustment after layout alignment, without changing the reserved size. */
    public ItemIcon offset(int x, int y) {
        offsetX = x;
        offsetY = y;
        return this;
    }

    @Override
    public void layout(LayoutContext ctx) {
        x = ctx.x + (centered ? Math.max(0, ctx.width - getWidth()) / 2 : 0) - visibleX + offsetX;
        y = ctx.y + (centered ? Math.max(0, ctx.height - getHeight()) / 2 : 0) - visibleY + offsetY;
    }

    @Override
    public void render(GumpBuilder g) {
        if (hue == 0) g.tilePic(x, y, tileId);
        else g.tilePicHue(x, y, tileId, hue);
        renderTooltip(g);
    }

    @Override
    public int getWidth() {
        return visibleWidth;
    }

    @Override
    public int getHeight() {
        return visibleHeight;
    }
}
