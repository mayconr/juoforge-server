package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.function.Function;

public class Label extends TooltipElement implements Sized {
    private final GumpText text;
    private int hue = 1152;
    private int font = 0xFF;
    private boolean horizontalCentered;
    private boolean verticallyCentered;
    private int x, y;

    public Label(String text) {
        this.text = new GumpText(text);
    }

    public Label(MessageContent text) {
        this.text = new GumpText(text);
    }

    /** Sets the UO hue sent in the gump text command. */
    public Label hue(int hue) {
        if (hue < 0 || hue > 0xFFFF) {
            throw new IllegalArgumentException("Label hue must fit an unsigned short");
        }
        this.hue = hue;
        return this;
    }

    /**
     * Sets an optional font ID; 0xFF preserves the client's automatic default.
     * Custom IDs are a layout extension and are ignored by unmodified ClassicUO.
     */
    public Label font(int font) {
        if (font < 0 || font > 0xFF) {
            throw new IllegalArgumentException("Label font must fit an unsigned byte");
        }
        this.font = font;
        return this;
    }

    /** Centers the label horizontally within its layout area using the current width estimate. */
    public Label horizontalCentered() {
        horizontalCentered = true;
        return this;
    }

    /** Centers the label vertically within its layout area using the current height estimate. */
    public Label verticallyCentered() {
        verticallyCentered = true;
        return this;
    }

    @Override
    public void resolveTexts(Function<MessageContent, String> resolver) {
        text.resolve(resolver);
    }

    @Override
    public void layout(LayoutContext ctx) {
        this.x = ctx.x + (horizontalCentered ? Math.max(0, ctx.width - getWidth()) / 2 : 0);
        this.y = ctx.y + (verticallyCentered ? Math.max(0, ctx.height - getHeight()) / 2 : 0);
    }

    @Override
    public void render(GumpBuilder g) {
        g.text(x, y, hue, text.value(), font);
        renderTooltip(g);
    }

    @Override
    public int getWidth() {
        return text.value().length() * 7; // rough estimate
    }

    @Override
    public int getHeight() {
        return 14;
    }
}
