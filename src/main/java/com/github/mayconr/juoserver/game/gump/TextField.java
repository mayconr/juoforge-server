package com.github.mayconr.juoserver.game.gump;

public class TextField implements UIElement, Sized {

    private static final int DEFAULT_BG = 3000; // gumpart padrão de campo

    private final int entryId;
    private final int width;
    private final int height;
    private final int hue;
    private final String initialText;
    private final boolean drawBackground;

    private int x, y;
    private int textIndex = -1;

    public TextField(int entryId, int width) {
        this(entryId, width, 24, 0, "", true);
    }

    public TextField(int entryId, int width, int height) {
        this(entryId, width, height, 0, "", true);
    }

    public TextField(
            int entryId,
            int width,
            int height,
            int hue,
            String initialText,
            boolean drawBackground) {
        this.entryId = entryId;
        this.width = width;
        this.height = height;
        this.hue = hue;
        this.initialText = initialText;
        this.drawBackground = drawBackground;
    }

    @Override
    public void layout(LayoutContext ctx) {
        this.x = ctx.x;
        this.y = ctx.y;
    }

    @Override
    public void render(GumpBuilder g) {

        // 1️⃣ background
        if (drawBackground) {
            g.resizePic(x, y, DEFAULT_BG, width, height);
        }

        // 2️⃣ texto inicial
        if (!initialText.isEmpty()) {
            textIndex = g.addText(initialText);
        }

        // 3️⃣ textentry (com padding interno)
        g.textEntry(x + 4, y + 4, width - 8, height - 8, hue, entryId, textIndex);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
