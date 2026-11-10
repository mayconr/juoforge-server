package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.function.Function;

public class Button extends TooltipElement implements Sized {
    private final int normalId;
    private final int pressedId;
    private int hoverId;
    private boolean captionCentered;
    private GumpText caption;
    private final int buttonId;
    private int x, y;

    public Button(int normalId, int pressedId, int buttonId, String caption) {
        this(normalId, pressedId, buttonId);
        this.caption = caption == null ? null : new GumpText(caption);
    }

    public Button(int normalId, int pressedId, int buttonId, MessageContent caption) {
        this(normalId, pressedId, buttonId);
        this.caption = new GumpText(caption);
    }

    public Button(int normalId, int pressedId, int buttonId) {
        this.normalId = normalId;
        this.pressedId = pressedId;
        this.buttonId = buttonId;
    }

    /**
     * Sets an optional mouse-over graphic (zero disables it).
     * This is a layout extension: the reference ClassicUO Button(List) parser ignores
     * the extra parameter and requires client support to display this graphic.
     */
    public Button hoverId(int hoverId) {
        if (hoverId < 0 || hoverId > 0xFFFF) {
            throw new IllegalArgumentException("Button hover graphic must fit an unsigned short");
        }
        this.hoverId = hoverId;
        return this;
    }

    /** Places the caption over the graphic, centered within the button's layout dimensions. */
    public Button captionCentered() {
        captionCentered = true;
        return this;
    }

    @Override
    public void resolveTexts(Function<MessageContent, String> resolver) {
        if (caption != null) caption.resolve(resolver);
    }

    @Override
    public void layout(LayoutContext ctx) {
        this.x = ctx.x;
        this.y = ctx.y;
    }

    @Override
    public void render(GumpBuilder g) {
        renderButton(g, x, y, normalId, pressedId, buttonId);
        renderTooltip(g);
        if (caption != null && !caption.value().isBlank()) {
            int captionX = captionCentered ? Math.max(0, getWidth() - caption.value().length() * 7) / 2 : 35;
            int captionY = captionCentered ? Math.max(0, getHeight() - 14) / 2 : 2;
            g.text(x + captionX, y + captionY, 1152, caption.value());
            renderTooltip(g);
        }
    }

    /** Emits the clickable control before its tooltip and optional caption. */
    protected void renderButton(GumpBuilder g, int x, int y, int normalId, int pressedId, int buttonId) {
        g.button(x, y, normalId, pressedId, true, 0, buttonId, hoverId);
    }

    @Override
    public int getWidth() {
        return 80;
    }

    @Override
    public int getHeight() {
        return 22;
    }
}
