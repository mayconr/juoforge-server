package com.github.mayconr.juoserver.game.ui.gump;

public class HtmlGump implements UIElement, Sized {

    private String value;
    private int x, y, width, height;
    private boolean background, scrollbar;

    public HtmlGump(String value, int width, int height) {
        this.value = value;
        this.width = width;
        this.height = height;
    }

    public HtmlGump(String value, int width, int height, boolean background, boolean scrollbar) {
        this(value, width, height);
        this.background = background;
        this.scrollbar = scrollbar;
    }

    @Override
    public void layout(LayoutContext ctx) {
        this.x = ctx.x;
        this.y = ctx.y;
    }

    @Override
    public void render(GumpBuilder g) {
        g.htmlGump(x, y, width, height, value, background, scrollbar);
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
