package com.github.mayconr.juoserver.game.ui.gump;

import java.util.List;

public class Column extends Container implements Sized, Alignable {
    private final int spacing;
    private boolean fillChildren;
    private VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
    private boolean fill;

    public Column(int spacing) {
        this(spacing, List.of());
    }

    public Column(int spacing, List<UIElement> children) {
        super(0, 0, 0, 0);
        this.spacing = spacing;
        this.children.addAll(List.copyOf(children));
    }

    /** Splits this column's height equally among its children and fills their width. */
    public Column fillChildren() {
        fillChildren = true;
        return this;
    }

    @Override
    public Column top() {
        verticalAlignment = VerticalAlignment.TOP;
        return this;
    }

    @Override
    public Column down() {
        verticalAlignment = VerticalAlignment.DOWN;
        return this;
    }

    @Override
    public Column left() {
        horizontalAlignment = HorizontalAlignment.LEFT;
        return this;
    }

    @Override
    public Column right() {
        horizontalAlignment = HorizontalAlignment.RIGHT;
        return this;
    }

    /** Centers the children vertically as a group. Ignored when this column uses fillChildren. */
    @Override
    public Column center() {
        verticalAlignment = VerticalAlignment.CENTER;
        return this;
    }

    @Override
    public Column add(UIElement element) {
        super.add(element);
        return this;
    }

    @Override
    public void layout(LayoutContext ctx) {
        fill = ctx.fill;
        x = ctx.x;
        y = ctx.y;
        width = Math.max(0, ctx.width);
        height = Math.max(0, ctx.height);
        int cy = ctx.y;
        if (fillChildren) {
            int remaining = Math.max(0, getHeight() - Math.max(0, children.size() - 1) * spacing);
            int count = children.size();
            for (UIElement element : children) {
                int childHeight = remaining / count;
                LayoutContext childContext = new LayoutContext(ctx.x, cy, getWidth(), childHeight);
                childContext.fill = true;
                element.layout(childContext);
                remaining -= childHeight;
                count--;
                cy += childHeight + spacing;
            }
            return;
        }
        cy += verticalAlignment.offset(ctx.height, getPreferredHeight());
        for (UIElement element : children) {
            int childHeight = element instanceof Sized sized ? sized.getPreferredHeight() : 48;
            int childWidth = element instanceof Sized sized ? sized.getPreferredWidth() : ctx.width;
            int childX = ctx.x + horizontalAlignment.offset(ctx.width, childWidth);
            element.layout(new LayoutContext(childX, cy,
                    horizontalAlignment == HorizontalAlignment.LEFT ? ctx.width : childWidth, childHeight));
            cy += childHeight + spacing;
        }
    }

    @Override
    public int getWidth() { return fill ? width : getPreferredWidth(); }

    @Override
    public int getHeight() { return fill ? height : getPreferredHeight(); }

    @Override
    public int getPreferredWidth() {
        return children.stream().mapToInt(e -> e instanceof Sized s ? s.getPreferredWidth() : 0).max().orElse(0);
    }

    @Override
    public int getPreferredHeight() {
        return children.stream().mapToInt(e -> e instanceof Sized s ? s.getPreferredHeight() : 48).sum()
                + Math.max(0, children.size() - 1) * spacing;
    }
}
