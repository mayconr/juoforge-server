package com.github.mayconr.juoserver.game.world.module.ui.gump;

class Button implements UIElement, Sized {
    private final int normalId;
    private final int pressedId;
    private String caption;
    private final int buttonId;
    private int x, y;

    public Button(int normalId, int pressedId, int buttonId, String caption) {
        this(normalId, pressedId, buttonId);
        this.caption = caption;
    }

    public Button(int normalId, int pressedId, int buttonId) {
        this.normalId = normalId;
        this.pressedId = pressedId;
        this.buttonId = buttonId;
    }

    @Override
    public void layout(LayoutContext ctx) {
        this.x = ctx.x;
        this.y = ctx.y;
    }

    @Override
    public void render(GumpBuilder g) {
        g.button(x, y, normalId, pressedId, true, 0, buttonId);
        if (caption != null && !caption.isBlank()) {
            g.text(x + 35, y + 2, 1152, caption);
        }
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
