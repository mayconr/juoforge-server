package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;

/** A reply button with item artwork, emitted as the standard buttontileart command. */
public final class ItemButton extends Button {
    private final int itemId;
    private int hue;
    private int itemX;
    private int itemY;

    public ItemButton(int normalId, int pressedId, int buttonId, int itemId) {
        super(normalId, pressedId, buttonId);
        validateGraphics(normalId, pressedId, itemId);
        this.itemId = itemId;
    }

    public ItemButton(int normalId, int pressedId, int buttonId, int itemId, String caption) {
        super(normalId, pressedId, buttonId, caption);
        validateGraphics(normalId, pressedId, itemId);
        this.itemId = itemId;
    }

    public ItemButton(int normalId, int pressedId, int buttonId, int itemId, MessageContent caption) {
        super(normalId, pressedId, buttonId, caption);
        validateGraphics(normalId, pressedId, itemId);
        this.itemId = itemId;
    }

    private static void validateGraphics(int normalId, int pressedId, int itemId) {
        if (normalId < 0 || normalId > 0xFFFF || pressedId < 0 || pressedId > 0xFFFF
                || itemId < 0 || itemId > 0xFFFF) {
            throw new IllegalArgumentException("Button graphics and item ID must fit unsigned shorts");
        }
    }

    /** Zero preserves the item's original colors. */
    public ItemButton hue(int hue) {
        if (hue < 0 || hue > 0xFFFF) throw new IllegalArgumentException("Hue must fit an unsigned short");
        this.hue = hue;
        return this;
    }

    /** Moves only the item artwork, relative to the button's origin. */
    public ItemButton itemOffset(int x, int y) {
        itemX = x;
        itemY = y;
        return this;
    }

    @Override
    public ItemButton captionCentered() {
        super.captionCentered();
        return this;
    }

    @Override
    public ItemButton itemProperty(int serialId) {
        super.itemProperty(serialId);
        return this;
    }

    @Override
    public ItemButton tooltip(String text) {
        super.tooltip(text);
        return this;
    }

    @Override
    public ItemButton tooltip(int clilocId, String... arguments) {
        super.tooltip(clilocId, arguments);
        return this;
    }

    @Override
    protected void renderButton(GumpBuilder g, int x, int y, int normalId, int pressedId, int buttonId) {
        g.buttonTileArt(x, y, normalId, pressedId, true, 0, buttonId, itemId, hue, itemX, itemY);
    }
}
