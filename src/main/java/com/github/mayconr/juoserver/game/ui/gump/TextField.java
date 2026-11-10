package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.network.packet.GumpSelection;
import java.util.function.Function;

public class TextField extends TooltipElement implements Sized {
    private boolean fill;
    private int layoutWidth, layoutHeight;

    private static final int DEFAULT_BG = 3000; // gumpart padrão de campo

    private final int entryId;
    private final int width;
    private final int height;
    private final int hue;
    private final GumpText initialText;
    private final boolean drawBackground;
    private Integer maxCharacters;

    private int x, y;
    private int textIndex = -1;

    public TextField(int entryId, int width) {
        this(entryId, width, 24, 0, "", true);
    }

    public TextField(int entryId, int width, int height) {
        this(entryId, width, height, 0, "", true);
    }

    /** Limits client input to between 1 and 239 UTF-16 code units. */
    public TextField(int entryId, int width, int height, int maxCharacters) {
        this(entryId, width, height, 0, "", true, maxCharacters);
    }

    public TextField(
            int entryId,
            int width,
            int height,
            int hue,
            String initialText,
            boolean drawBackground) {
        this(entryId, width, height, hue, new GumpText(initialText), drawBackground);
    }

    public TextField(int entryId, int width, int height, int hue, MessageContent initialText, boolean drawBackground) {
        this(entryId, width, height, hue, new GumpText(initialText), drawBackground);
    }

    public TextField(int entryId, int width, int height, int hue, String initialText,
                     boolean drawBackground, int maxCharacters) {
        this(entryId, width, height, hue, new GumpText(initialText), drawBackground, maxCharacters);
    }

    public TextField(int entryId, int width, int height, int hue, MessageContent initialText,
                     boolean drawBackground, int maxCharacters) {
        this(entryId, width, height, hue, new GumpText(initialText), drawBackground, maxCharacters);
    }

    private TextField(int entryId, int width, int height, int hue, GumpText initialText, boolean drawBackground) {
        this(entryId, width, height, hue, initialText, drawBackground, null);
    }

    private TextField(int entryId, int width, int height, int hue, GumpText initialText,
                      boolean drawBackground, Integer maxCharacters) {
        this.entryId = entryId;
        this.width = width;
        this.height = height;
        this.hue = hue;
        this.initialText = initialText;
        this.drawBackground = drawBackground;
        if (maxCharacters != null) maxCharacters(maxCharacters);
    }

    /** Sets the optional client input limit, in UTF-16 code units. */
    public TextField maxCharacters(int maxCharacters) {
        if (maxCharacters < 1 || maxCharacters > GumpSelection.MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("Text entry limit must be between 1 and " + GumpSelection.MAX_TEXT_LENGTH);
        }
        this.maxCharacters = maxCharacters;
        return this;
    }

    @Override
    public void resolveTexts(Function<MessageContent, String> resolver) {
        initialText.resolve(resolver);
    }

    @Override
    public void layout(LayoutContext ctx) {
        fill = ctx.fill;
        layoutWidth = Math.max(0, ctx.width);
        layoutHeight = Math.max(0, ctx.height);
        this.x = ctx.x;
        this.y = ctx.y;
    }

    @Override
    public void render(GumpBuilder g) {

        // 1️⃣ background
        if (drawBackground) {
            g.resizePic(x, y, DEFAULT_BG, getWidth(), getHeight());
            renderTooltip(g);
        }

        textIndex = -1;

        // 2️⃣ texto inicial
        if (!initialText.value().isEmpty()) {
            textIndex = g.addText(initialText.value());
        }

        // 3️⃣ textentry (com padding interno)
        if (maxCharacters == null) {
            g.textEntry(x + 4, y + 4, getWidth() - 8, getHeight() - 8, hue, entryId, textIndex);
        } else {
            g.textEntryLimited(x + 4, y + 4, getWidth() - 8, getHeight() - 8,
                    hue, entryId, textIndex, maxCharacters);
        }
        renderTooltip(g);
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
