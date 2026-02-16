package com.github.mayconr.juoserver.game.world.module.ui.gump;

public class ItemIcon implements UIElement, Sized {
    private int x, y;
    private final int tileId;

    public ItemIcon(int tileId) {
        this.tileId = tileId;
    }

    @Override
    public void layout(LayoutContext ctx) {
        x = ctx.x;
        y = ctx.y;
    }

    @Override
    public void render(GumpBuilder g) {
        g.tilePic(x, y, tileId);
    }

    @Override
    public int getWidth() {
        return 30;
    }

    @Override
    public int getHeight() {
        return 30;
    }
}
