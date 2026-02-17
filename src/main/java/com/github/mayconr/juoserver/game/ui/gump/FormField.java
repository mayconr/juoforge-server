package com.github.mayconr.juoserver.game.ui.gump;

public class FormField implements UIElement {

    private final UIElement label;
    private final UIElement field;
    private final boolean inline;
    private final int gap;

    public FormField(UIElement label, UIElement field, boolean inline, int gap) {
        this.label = label;
        this.field = field;
        this.inline = inline;
        this.gap = gap;
    }

    public static FormField stacked(UIElement label, UIElement field) {
        return new FormField(label, field, false, 4);
    }

    public static FormField inline(UIElement label, UIElement field) {
        return new FormField(label, field, true, 8);
    }

    @Override
    public void layout(LayoutContext ctx) {

        if (!inline) {
            // Label em cima
            LayoutContext labelCtx = new LayoutContext(ctx.x, ctx.y, ctx.width, 20);
            label.layout(labelCtx);

            LayoutContext fieldCtx = new LayoutContext(ctx.x, ctx.y + 22, ctx.width, 24);
            field.layout(fieldCtx);
        } else {
            // Label + campo lado a lado
            int labelWidth = 80;

            LayoutContext labelCtx = new LayoutContext(ctx.x, ctx.y, labelWidth, 24);
            label.layout(labelCtx);

            LayoutContext fieldCtx =
                    new LayoutContext(
                            ctx.x + labelWidth + gap, ctx.y, ctx.width - labelWidth - gap, 24);
            field.layout(fieldCtx);
        }
    }

    @Override
    public void render(GumpBuilder g) {
        label.render(g);
        field.render(g);
    }
}
