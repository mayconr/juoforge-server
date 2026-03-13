package com.github.mayconr.juoserver.game.ui.gump;

class Checkbox implements UIElement, Sized {

    private final int uncheckedId;
    private final int checkedId;
    private final int switchId;
    private final boolean checked;
    private int x, y;

    Checkbox(int uncheckedId, int checkedId, int switchId, boolean checked) {
        this.uncheckedId = uncheckedId;
        this.checkedId = checkedId;
        this.switchId = switchId;
        this.checked = checked;
    }

    @Override
    public void layout(LayoutContext ctx) {
        x = ctx.x;
        y = ctx.y;
    }

    @Override
    public void render(GumpBuilder g) {
        g.checkbox(x, y, uncheckedId, checkedId, checked, switchId);
    }

    @Override
    public int getWidth() {
        return 20;
    }

    @Override
    public int getHeight() {
        return 20;
    }
}
