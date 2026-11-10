package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.function.Function;

public class ItemSlot extends TooltipElement implements Sized {
    private boolean fill;
    private int layoutWidth, layoutHeight;

    private final UIElement content;
    private final int width;
    private final int height;
    private final int backgroundId;
    private int x, y;

    ItemSlot(int size, UIElement content) {
        this(size, size, content);
    }

    ItemSlot(int width, int height, UIElement content) {
        this(3000, width, height, content);
    }

    ItemSlot(int backgroundId, int width, int height, UIElement content) {
        if (backgroundId < 0 || backgroundId > 0xFFFF)
            throw new IllegalArgumentException("Background ID must fit an unsigned short");
        if (width < 8 || height < 8) throw new IllegalArgumentException("Item slot must accommodate its padding");
        this.backgroundId = backgroundId;
        this.width = width;
        this.height = height;
        this.content = content;
    }

    @Override
    public void resolveTexts(Function<MessageContent, String> resolver) {
        content.resolveTexts(resolver);
    }

    @Override
    public void layout(LayoutContext ctx) {
        fill = ctx.fill;
        layoutWidth = Math.max(0, ctx.width);
        layoutHeight = Math.max(0, ctx.height);
        x = ctx.x;
        y = ctx.y;

        // Center the actual content bounds, including art larger than the frame.
        // Clamping the offset to zero would leave all overflow on the right/bottom.
        LayoutContext inner = content instanceof Sized sized
                ? new LayoutContext(x + Math.floorDiv(getWidth() - sized.getWidth(), 2),
                        y + Math.floorDiv(getHeight() - sized.getHeight(), 2),
                        sized.getWidth(), sized.getHeight())
                : new LayoutContext(x + 4, y + 4, getWidth() - 8, getHeight() - 8);

        content.layout(inner);
    }

    @Override
    public void render(GumpBuilder g) {
        g.resizePic(x, y, backgroundId, getWidth(), getHeight());
        renderTooltip(g);
        content.render(g);
    }

    @Override
    public int getWidth() {
        return fill ? layoutWidth : width;
    }

    @Override
    public int getHeight() {
        return fill ? layoutHeight : height;
    }

    @Override
    public int getPreferredWidth() { return width; }

    @Override
    public int getPreferredHeight() { return height; }
}
