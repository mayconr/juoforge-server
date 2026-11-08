package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.function.Function;

public class FormField implements UIElement, Sized {

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
    public void resolveTexts(Function<MessageContent, String> resolver) {
        label.resolveTexts(resolver);
        field.resolveTexts(resolver);
    }

    @Override
    public void layout(LayoutContext ctx) {

        if (!inline) {
            // Label em cima
            LayoutContext labelCtx = new LayoutContext(ctx.x, ctx.y, ctx.width, labelHeight());
            label.layout(labelCtx);

            LayoutContext fieldCtx = new LayoutContext(ctx.x, ctx.y + labelHeight() + gap, ctx.width, fieldHeight());
            field.layout(fieldCtx);
        } else {
            // Label + campo lado a lado
            int labelWidth = 80;

            LayoutContext labelCtx = new LayoutContext(ctx.x, ctx.y, labelWidth, labelHeight());
            label.layout(labelCtx);

            LayoutContext fieldCtx =
                    new LayoutContext(
                            ctx.x + labelWidth + gap, ctx.y, Math.max(0, ctx.width - labelWidth - gap), fieldHeight());
            field.layout(fieldCtx);
        }
    }

    @Override
    public void render(GumpBuilder g) {
        label.render(g);
        field.render(g);
    }
    private int labelHeight() { return label instanceof Sized s ? s.getHeight() : 20; }
    private int fieldHeight() { return field instanceof Sized s ? s.getHeight() : 24; }

    @Override
    public int getWidth() {
        int labelWidth = label instanceof Sized s ? s.getWidth() : 80;
        int fieldWidth = field instanceof Sized s ? s.getWidth() : 0;
        return inline ? 80 + gap + fieldWidth : Math.max(labelWidth, fieldWidth);
    }

    @Override
    public int getHeight() {
        return inline ? Math.max(labelHeight(), fieldHeight()) : labelHeight() + gap + fieldHeight();
    }
}
