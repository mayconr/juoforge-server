package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.function.Function;

public class HtmlGump extends TooltipElement implements Sized {
    private boolean fill;
    private int layoutWidth, layoutHeight;

    private final GumpText value;
    private final Integer clilocId;
    private Integer color;
    private String[] arguments;
    private int x, y, width, height;
    private boolean background, scrollbar;

    public HtmlGump(String value, int width, int height) {
        this.value = new GumpText(value);
        this.clilocId = null;
        this.width = width;
        this.height = height;
    }

    public HtmlGump(MessageContent value, int width, int height) {
        this.value = new GumpText(value);
        this.clilocId = null;
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

    /** The client resolves this cliloc; no text is added to the server's gump text table. */
    public HtmlGump(int clilocId, int width, int height) {
        this(clilocId, width, height, false, false);
    }

    public HtmlGump(int clilocId, int width, int height, boolean background, boolean scrollbar) {
        if (clilocId <= 0) throw new IllegalArgumentException("Cliloc ID must be positive");
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Cliloc HTML dimensions must be positive");
        this.clilocId = clilocId;
        value = null;
        this.width = width;
        this.height = height;
        this.background = background;
        this.scrollbar = scrollbar;
    }

    /** Sets a packed UO HTML color, rather than an indexed item hue. */
    public HtmlGump color(int color) {
        requireCliloc();
        GumpBuilder.validateHtmlColor(color);
        this.color = color;
        return this;
    }

    /** Enables xmfhtmltok, using tab-separated arguments resolved by the client. */
    public HtmlGump arguments(String... arguments) {
        requireCliloc();
        GumpBuilder.clilocArguments(arguments);
        this.arguments = arguments.clone();
        return this;
    }

    private void requireCliloc() {
        if (clilocId == null) throw new IllegalStateException("Color and cliloc arguments require a cliloc HTML element");
    }

    @Override
    public void resolveTexts(Function<MessageContent, String> resolver) {
        if (value != null) value.resolve(resolver);
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
        if (clilocId == null) {
            g.htmlGump(x, y, getWidth(), getHeight(), value.value(), background, scrollbar);
        } else {
            if (getWidth() <= 0 || getHeight() <= 0) return;
            if (arguments != null) {
                g.xmfHtmlTok(x, y, getWidth(), getHeight(), clilocId, background, scrollbar,
                        color == null ? 0 : color, arguments);
            } else if (color != null) {
                g.xmfHtmlGumpColor(x, y, getWidth(), getHeight(), clilocId, background, scrollbar, color);
            } else {
                g.xmfHtmlGump(x, y, getWidth(), getHeight(), clilocId, background, scrollbar);
            }
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
