package com.github.mayconr.juoserver.game.ui.gump;

class ItemSlot implements UIElement, Sized {

    private final UIElement content;
    private final int size;
    private int x, y;

    ItemSlot(int size, UIElement content) {
        this.size = size;
        this.content = content;
    }

    @Override
    public void layout(LayoutContext ctx) {
        x = ctx.x;
        y = ctx.y;

        LayoutContext inner = new LayoutContext(x + 4, y + 4, size - 8, size - 8);

        content.layout(inner);
    }

    @Override
    public void render(GumpBuilder g) {
        g.resizePic(x, y, 3000, size, size);
        content.render(g);
    }

    @Override
    public int getWidth() {
        return size;
    }

    @Override
    public int getHeight() {
        return size;
    }
}
