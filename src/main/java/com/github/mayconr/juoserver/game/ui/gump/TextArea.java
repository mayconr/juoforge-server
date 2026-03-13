package com.github.mayconr.juoserver.game.ui.gump;

class TextArea implements UIElement, Sized {

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
        x = ctx.x;
        y = ctx.y;
    }

    @Override
    public void render(GumpBuilder g) {
        g.resizePic(x, y, 3000, width, height);
        g.textEntry(x + 4, y + 4, width - 8, height - 8, 0, entryId, -1);
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
