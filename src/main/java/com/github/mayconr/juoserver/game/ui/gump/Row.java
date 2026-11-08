package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.function.Function;

import java.util.List;

public class Row implements UIElement, Sized {
    private final List<UIElement> children;
    private final int gap;

    public Row(int gap, List<UIElement> children) {
        this.gap = gap;
        this.children = List.copyOf(children);
    }

    @Override
    public void resolveTexts(Function<MessageContent, String> resolver) {
        for (UIElement child : children) child.resolveTexts(resolver);
    }

    @Override
    public void layout(LayoutContext ctx) {
        int x = ctx.x;
        int unsized = (int) children.stream().filter(e -> !(e instanceof Sized)).count();
        int remaining = Math.max(0, ctx.width - getWidth());
        for (UIElement child : children) {
            int width = child instanceof Sized s ? s.getWidth() : remaining / Math.max(1, unsized);
            int height = child instanceof Sized s ? s.getHeight() : 48;
            child.layout(new LayoutContext(x, ctx.y, width, height));
            x += width + gap;
        }
    }

    @Override
    public void render(GumpBuilder g) {
        for (UIElement child : children) child.render(g);
    }

    @Override
    public int getWidth() {
        return children.stream().mapToInt(e -> e instanceof Sized s ? s.getWidth() : 0).sum()
                + Math.max(0, children.size() - 1) * gap;
    }

    @Override
    public int getHeight() {
        return children.stream().mapToInt(e -> e instanceof Sized s ? s.getHeight() : 48).max().orElse(0);
    }
}
