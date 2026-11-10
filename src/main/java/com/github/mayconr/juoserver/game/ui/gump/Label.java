package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.function.Function;

public class Label extends TooltipElement implements Sized, Alignable {
    private final GumpText text;
    private int hue = 1152;
    private int font = 0xFF;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
    private VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
    private int x, y;
    private boolean fill;
    private int layoutWidth, layoutHeight;
    private Integer croppedWidth;
    private int croppedHeight;

    public Label(String text) {
        this.text = new GumpText(text);
    }

    public Label(MessageContent text) {
        this.text = new GumpText(text);
    }

    /** Reserves a text box and requests client-side width cropping through croppedtext. */
    public Label cropped(int width, int height) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Cropped text dimensions must be positive");
        croppedWidth = width;
        croppedHeight = height;
        return this;
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

    @Override
    public Label top() {
        verticalAlignment = VerticalAlignment.TOP;
        return this;
    }

    @Override
    public Label down() {
        verticalAlignment = VerticalAlignment.DOWN;
        return this;
    }

    @Override
    public Label left() {
        horizontalAlignment = HorizontalAlignment.LEFT;
        return this;
    }

    @Override
    public Label right() {
        horizontalAlignment = HorizontalAlignment.RIGHT;
        return this;
    }

    /** Centers the label on both axes within its layout area. */
    @Override
    public Label center() {
        return horizontalCentered().verticallyCentered();
    }

    /** Centers the label horizontally within its layout area using the current width estimate. */
    public Label horizontalCentered() {
        horizontalAlignment = HorizontalAlignment.CENTER;
        return this;
    }

    /** Centers the label vertically within its layout area using the current height estimate. */
    public Label verticallyCentered() {
        verticalAlignment = VerticalAlignment.CENTER;
        return this;
    }

    @Override
    public void resolveTexts(Function<MessageContent, String> resolver) {
        text.resolve(resolver);
    }

    @Override
    public void layout(LayoutContext ctx) {
        fill = ctx.fill;
        layoutWidth = Math.max(0, ctx.width);
        layoutHeight = Math.max(0, ctx.height);
        int contentWidth = croppedWidth == null ? getPreferredWidth() : getWidth();
        int contentHeight = croppedWidth == null ? getPreferredHeight() : getHeight();
        this.x = ctx.x + horizontalAlignment.offset(ctx.width, contentWidth);
        this.y = ctx.y + verticalAlignment.offset(ctx.height, contentHeight);
    }

    @Override
    public void render(GumpBuilder g) {
        if (croppedWidth == null) {
            g.text(x, y, hue, text.value(), font);
        } else {
            if (getWidth() <= 0 || getHeight() <= 0) return;
            g.croppedText(x, y, getWidth(), getHeight(), hue, text.value());
        }
        renderTooltip(g);
    }

    @Override
    public int getWidth() {
        return fill ? layoutWidth : getPreferredWidth();
    }

    @Override
    public int getHeight() {
        return fill ? layoutHeight : getPreferredHeight();
    }
    @Override
    public int getPreferredWidth() { return croppedWidth == null ? text.value().length() * 7 : croppedWidth; }

    @Override
    public int getPreferredHeight() { return croppedWidth == null ? 14 : croppedHeight; }
}
