package com.github.mayconr.juoserver.game.ui.gump;

public class Column extends Container implements Sized {
    private final int spacing;

    public Column(int spacing) {
        super(0, 0, 0, 0);
        this.spacing = spacing;
    }

    @Override
    public void layout(LayoutContext ctx) {
        int cy = ctx.y;
        for (UIElement element : children) {
            int height = element instanceof Sized sized ? sized.getHeight() : 48;
            element.layout(new LayoutContext(ctx.x, cy, ctx.width, height));
            cy += height + spacing;
        }
    }

    @Override
    public int getWidth() {
        return children.stream().mapToInt(e -> e instanceof Sized s ? s.getWidth() : 0).max().orElse(0);
    }

    @Override
    public int getHeight() {
        return children.stream().mapToInt(e -> e instanceof Sized s ? s.getHeight() : 48).sum()
                + Math.max(0, children.size() - 1) * spacing;
    }
}
