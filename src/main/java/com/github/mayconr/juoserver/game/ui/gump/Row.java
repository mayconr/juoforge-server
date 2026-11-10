package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.function.Function;
import java.util.List;

public class Row implements Alignable, Sized {
    private final List<UIElement> children;
    private final int gap;
    private boolean fillChildren;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
    private VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
    private boolean fill;
    private int layoutWidth;
    private int layoutHeight;

    public Row(int gap, List<UIElement> children) {
        this.gap = gap;
        this.children = List.copyOf(children);
    }

    /** Splits this row's width equally among its children and fills their height. */
    public Row fillChildren() {
        fillChildren = true;
        return this;
    }

    @Override
    public Row top() {
        verticalAlignment = VerticalAlignment.TOP;
        return this;
    }

    @Override
    public Row down() {
        verticalAlignment = VerticalAlignment.DOWN;
        return this;
    }

    @Override
    public Row left() {
        horizontalAlignment = HorizontalAlignment.LEFT;
        return this;
    }

    @Override
    public Row right() {
        horizontalAlignment = HorizontalAlignment.RIGHT;
        return this;
    }

    /** Centers the children horizontally as a group. Ignored when this row uses fillChildren. */
    @Override
    public Row center() {
        horizontalAlignment = HorizontalAlignment.CENTER;
        return this;
    }

    @Override
    public void resolveTexts(Function<MessageContent, String> resolver) {
        for (UIElement child : children) child.resolveTexts(resolver);
    }

    @Override
    public void layout(LayoutContext ctx) {
        fill = ctx.fill;
        layoutWidth = Math.max(0, ctx.width);
        layoutHeight = Math.max(0, ctx.height);
        int x = ctx.x;
        if (fillChildren) {
            int remaining = Math.max(0, getWidth() - Math.max(0, children.size() - 1) * gap);
            int count = children.size();
            for (UIElement child : children) {
                int childWidth = remaining / count;
                LayoutContext childContext = new LayoutContext(x, ctx.y, childWidth, getHeight());
                childContext.fill = true;
                child.layout(childContext);
                remaining -= childWidth;
                count--;
                x += childWidth + gap;
            }
            return;
        }
        int unsized = (int) children.stream().filter(e -> !(e instanceof Sized)).count();
        int remaining = Math.max(0, ctx.width - getPreferredWidth());
        if (unsized == 0) {
            x += horizontalAlignment.offset(ctx.width, getPreferredWidth());
        }
        for (UIElement child : children) {
            int childWidth = child instanceof Sized sized
                    ? sized.getPreferredWidth() : remaining / Math.max(1, unsized);
            int childHeight = child instanceof Sized sized ? sized.getPreferredHeight() : 48;
            int childY = ctx.y + verticalAlignment.offset(ctx.height, childHeight);
            child.layout(new LayoutContext(x, childY, childWidth, childHeight));
            x += childWidth + gap;
        }
    }

    @Override
    public void render(GumpBuilder g) {
        for (UIElement child : children) child.render(g);
    }

    @Override
    public int getWidth() { return fill ? layoutWidth : getPreferredWidth(); }

    @Override
    public int getHeight() { return fill ? layoutHeight : getPreferredHeight(); }

    @Override
    public int getPreferredWidth() {
        return children.stream().mapToInt(e -> e instanceof Sized s ? s.getPreferredWidth() : 0).sum()
                + Math.max(0, children.size() - 1) * gap;
    }

    @Override
    public int getPreferredHeight() {
        return children.stream().mapToInt(e -> e instanceof Sized s ? s.getPreferredHeight() : 48).max().orElse(0);
    }
}
