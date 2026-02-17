package com.github.mayconr.juoserver.game.ui.gump;

class Column extends Container {
    private final int spacing;

    public Column(int spacing) {
        super(0, 0, 0, 0);
        this.spacing = spacing;
    }

    @Override
    public void layout(LayoutContext ctx) {
        int cy = ctx.y;
        for (UIElement e : children) {
            e.layout(new LayoutContext(ctx.x, cy, ctx.width, ctx.height));
            if (e instanceof Sized s) cy += s.getHeight() + spacing;
        }
    }
}
