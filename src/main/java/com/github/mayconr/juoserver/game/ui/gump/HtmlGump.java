package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.function.Function;

public class HtmlGump extends TooltipElement implements Sized {

    private final GumpText value;
    private int x, y, width, height;
    private boolean background, scrollbar;

    public HtmlGump(String value, int width, int height) {
        this.value = new GumpText(value);
        this.width = width;
        this.height = height;
    }

    public HtmlGump(MessageContent value, int width, int height) {
        this.value = new GumpText(value);
        this.width = width;
        this.height = height;
    }

    public HtmlGump(String value, int width, int height, boolean background, boolean scrollbar) {
        this(value, width, height);
        this.background = background;
        this.scrollbar = scrollbar;
    }

    public HtmlGump(MessageContent value, int width, int height, boolean background, boolean scrollbar) {
        this(value, width, height);
        this.background = background;
        this.scrollbar = scrollbar;
    }

    @Override
    public void resolveTexts(Function<MessageContent, String> resolver) {
        value.resolve(resolver);
    }

    @Override
    public void layout(LayoutContext ctx) {
        this.x = ctx.x;
        this.y = ctx.y;
    }

    @Override
    public void render(GumpBuilder g) {
        g.htmlGump(x, y, width, height, value.value(), background, scrollbar);
        renderTooltip(g);
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
