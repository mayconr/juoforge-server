package com.github.mayconr.juoserver.game.world.module.ui.gump;

public final class PageButton implements UIElement {

    private final int normalId;
    private final int pressedId;
    private final int targetPage;

    private int x;
    private int y;

    public PageButton(int normalId, int pressedId, int targetPage) {
        this.normalId = normalId;
        this.pressedId = pressedId;
        this.targetPage = targetPage;
    }

    @Override
    public void layout(LayoutContext ctx) {
        this.x = ctx.x;
        this.y = ctx.y;
    }

    @Override
    public void render(GumpBuilder g) {
        g.pageButton(x, y, normalId, pressedId, targetPage);
    }
}
