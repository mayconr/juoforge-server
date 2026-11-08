package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.function.Function;

public class Panel extends TooltipElement implements Sized {

    private final int width;
    private final int height;
    private final Integer gumPicId; // null = sem background
    private final boolean resizable;
    private final UIElement content;

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

    /** A panel tooltip targets its background; a background must be configured. */
    @Override
    public void resolveTexts(Function<MessageContent, String> resolver) {
        content.resolveTexts(resolver);
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

        // padding
        int padding = gumPicId != null ? 10 : 0;

        LayoutContext inner =
                ctx.child(x + padding, y + padding, width - padding * 2, height - padding * 2);

        content.layout(inner);
    }

    @Override
    public void render(GumpBuilder g) {
        if (gumPicId != null) {
            if (resizable) {
                g.resizePic(x, y, gumPicId, width, height);
                renderTooltip(g);
            } else {
                g.gumpPic(x, y, gumPicId);
                renderTooltip(g);
            }
        }

        content.render(g);
    }
    @Override
    public int getWidth() { return width; }

    @Override
    public int getHeight() { return height; }
}
