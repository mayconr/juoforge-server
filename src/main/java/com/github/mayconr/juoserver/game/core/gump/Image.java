package com.github.mayconr.juoserver.game.core.gump;

class Image implements UIElement, Sized {

    private final int gumpPicId;
    private int x, y, width, height;

    public Image(int gumpPicId, int width, int height) {
        this.gumpPicId = gumpPicId;
        this.width = width;
        this.height = height;
    }

    @Override
    public void layout(LayoutContext ctx) {
        this.x = ctx.x;
        this.y = ctx.y;
    }

    @Override
    public void render(GumpBuilder g) {
        g.gumpPic(x, y, gumpPicId);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
