package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.function.Function;

public class Panel extends TooltipElement implements Sized, Alignable {

    private final int width;
    private final int height;
    private final Integer gumPicId; // null = sem background
    private final boolean resizable;
    private final UIElement content;
    private VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
    private boolean fillChildren;
    private boolean fill;
    private int layoutWidth;
    private int layoutHeight;

    private int x, y;

    public Panel(int width, int height, UIElement content) {
        this(null, width, height, true, content);
    }

    // Panel COM background
    public Panel(Integer gumPicId, int width, int height, UIElement content) {
        this(gumPicId, width, height, true, content);
    }

    public Panel(Integer gumPicId, int width, int height, boolean resizable, UIElement content) {
        this.width = width;
        this.height = height;
        this.gumPicId = gumPicId;
        this.resizable = resizable;
        this.content = content;
    }

    /** Makes the content fill this panel's inner area on both axes. */
    public Panel fillChildren() {
        fillChildren = true;
        return this;
    }

    @Override
    public Panel top() {
        if (!(content instanceof Sized)) {
            throw new IllegalStateException("Aligning panel content requires a sized element");
        }
        verticalAlignment = VerticalAlignment.TOP;
        return this;
    }

    @Override
    public Panel down() {
        if (!(content instanceof Sized)) {
            throw new IllegalStateException("Aligning panel content requires a sized element");
        }
        verticalAlignment = VerticalAlignment.DOWN;
        return this;
    }

    @Override
    public Panel left() {
        if (!(content instanceof Sized)) {
            throw new IllegalStateException("Aligning panel content requires a sized element");
        }
        horizontalAlignment = HorizontalAlignment.LEFT;
        return this;
    }

    @Override
    public Panel right() {
        if (!(content instanceof Sized)) {
            throw new IllegalStateException("Aligning panel content requires a sized element");
        }
        horizontalAlignment = HorizontalAlignment.RIGHT;
        return this;
    }

    /** Centers sized content on both axes. Ignored when this panel uses fillChildren. */
    @Override
    public Panel center() {
        if (!(content instanceof Sized)) {
            throw new IllegalStateException("Centering panel content requires a sized element");
        }
        verticalAlignment = VerticalAlignment.CENTER;
        horizontalAlignment = HorizontalAlignment.CENTER;
        return this;
    }

    /** A panel tooltip targets its background; a background must be configured. */
    @Override
    public void resolveTexts(Function<MessageContent, String> resolver) {
        content.resolveTexts(resolver);
    }

    @Override
    public Panel itemProperty(int serialId) {
        if (gumPicId == null) {
            throw new IllegalStateException("A panel needs a background to display its own item properties");
        }
        super.itemProperty(serialId);
        return this;
    }

    @Override
    public Panel tooltip(String text) {
        super.tooltip(text);
        return this;
    }

    @Override
    public Panel tooltip(int clilocId, String... arguments) {
        if (gumPicId == null) {
            throw new IllegalStateException("A panel needs a background to display its own tooltip");
        }
        super.tooltip(clilocId, arguments);
        return this;
    }

    @Override
    public void layout(LayoutContext ctx) {
        this.x = ctx.x;
        this.y = ctx.y;
        fill = ctx.fill;
        layoutWidth = Math.max(0, ctx.width);
        layoutHeight = Math.max(0, ctx.height);

        // padding
        int padding = gumPicId != null ? 10 : 0;

        LayoutContext inner =
                ctx.child(x + padding, y + padding,
                        Math.max(0, getWidth() - padding * 2), Math.max(0, getHeight() - padding * 2));

        inner.fill = fillChildren;

        if (!fillChildren && (verticalAlignment != VerticalAlignment.TOP || horizontalAlignment != HorizontalAlignment.LEFT) && content instanceof Sized sized) {
            int contentWidth = sized.getPreferredWidth();
            int contentHeight = sized.getPreferredHeight();
            inner = inner.child(
                    inner.x + horizontalAlignment.offset(inner.width, contentWidth),
                    inner.y + verticalAlignment.offset(inner.height, contentHeight),
                    contentWidth, contentHeight);
        }

        content.layout(inner);
    }

    @Override
    public void render(GumpBuilder g) {
        if (gumPicId != null) {
            if (resizable || fill) {
                g.resizePic(x, y, gumPicId, getWidth(), getHeight());
                renderTooltip(g);
            } else {
                g.gumpPic(x, y, gumPicId);
                renderTooltip(g);
            }
        }

        content.render(g);
    }
    @Override
    public int getWidth() { return fill ? layoutWidth : width; }

    @Override
    public int getHeight() { return fill ? layoutHeight : height; }

    @Override
    public int getPreferredWidth() { return width; }

    @Override
    public int getPreferredHeight() { return height; }
}
