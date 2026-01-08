package com.github.mayconr.juoserver.game.core.gump;

public class Radio implements UIElement, Sized {

    private final int uncheckedId;
    private final int checkedId;
    private final int switchId;
    private boolean checked;

    private int x, y;

    public Radio(int uncheckedId, int checkedId, int switchId, boolean checked) {
        this.uncheckedId = uncheckedId;
        this.checkedId = checkedId;
        this.switchId = switchId;
        this.checked = checked;
    }

    public Radio(int uncheckedId, int checkedId, int switchId) {
        this(uncheckedId, checkedId, switchId, false);
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public void layout(LayoutContext ctx) {
        this.x = ctx.x;
        this.y = ctx.y;
    }

    @Override
    public void render(GumpBuilder g) {
        g.radio(x, y, uncheckedId, checkedId, checked, switchId);
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
