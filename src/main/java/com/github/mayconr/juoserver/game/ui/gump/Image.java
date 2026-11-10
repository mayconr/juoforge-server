package com.github.mayconr.juoserver.game.ui.gump;

public class Image extends TooltipElement implements Sized {

    private final int gumpPicId;
    private int x, y, width, height;
    private Integer hue;
    private boolean partialHue;
    private boolean cropped;
    private int sourceX, sourceY, croppedWidth, croppedHeight;

    public Image(int gumpPicId, int width, int height) {
        this.gumpPicId = gumpPicId;
        this.width = width;
        this.height = height;
    }

    /** Selects an unscaled region of the original artwork and reserves its dimensions in the layout. */
    public Image cropped(int sourceX, int sourceY, int width, int height) {
        GumpBuilder.validateImageRegion(gumpPicId, sourceX, sourceY, width, height);
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        croppedWidth = width;
        croppedHeight = height;
        cropped = true;
        return this;
    }

    /** Requests full hue using the selected client command's hue encoding. */
    public Image hue(int hue) {
        validateHue(hue);
        this.hue = hue;
        partialHue = false;
        return this;
    }

    /** Requests partial hue. Uncropped images require client support for gumppicphued's partial rendering. */
    public Image partialHue(int hue) {
        validateHue(hue);
        this.hue = hue;
        partialHue = true;
        return this;
    }

    private static void validateHue(int hue) {
        if (hue < 0 || hue > 0xFFFF) throw new IllegalArgumentException("Hue must fit an unsigned short");
    }

    @Override
    public void layout(LayoutContext ctx) {
        this.x = ctx.x;
        this.y = ctx.y;
    }

    @Override
    public void render(GumpBuilder g) {
        if (cropped) {
            if (hue == null) g.picInPic(x, y, gumpPicId, sourceX, sourceY, croppedWidth, croppedHeight);
            else if (partialHue) g.picInPicPartialHued(x, y, gumpPicId, sourceX, sourceY, croppedWidth, croppedHeight, hue);
            else g.picInPicHued(x, y, gumpPicId, sourceX, sourceY, croppedWidth, croppedHeight, hue);
        } else if (hue == null) g.gumpPic(x, y, gumpPicId);
        else if (partialHue) g.gumpPicPartialHued(x, y, gumpPicId, hue);
        else g.gumpPicHued(x, y, gumpPicId, hue);
        renderTooltip(g);
    }

    @Override
    public int getWidth() {
        return cropped ? croppedWidth : width;
    }

    @Override
    public int getHeight() {
        return cropped ? croppedHeight : height;
    }
}
