package com.github.mayconr.juoserver.game.world.module.ui.gump;

class Label implements UIElement, Sized {
    private final String text;
    private int x, y;

    public Label(String text) {
        this.text = text;
    }

    @Override
    public void layout(LayoutContext ctx) {
        this.x = ctx.x;
        this.y = ctx.y;
    }

    @Override
    public void render(GumpBuilder g) {
        g.text(x, y, 1152, text);
    }

    @Override
    public int getWidth() {
        return text.length() * 7; // rough estimate
    }

    @Override
    public int getHeight() {
        return 14;
    }
}
