package com.github.mayconr.juoserver.game.ui.gump;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GumpBuilder {

    private final StringBuilder layout = new StringBuilder();
    private final List<String> texts = new ArrayList<>();
    private int currentPage = -1;

    /* =========================
     *  Core helpers
     * ========================= */

    private void requirePage() {
        if (currentPage < 0)
            throw new IllegalStateException("No active page. Call page(n) with n >= 0 first.");
    }

    private void line(String cmd) {
        layout.append('{').append(cmd).append('}').append('\n');
    }

    public int addText(String text) {
        int idx = texts.size();
        texts.add(Objects.requireNonNull(text));
        return idx;
    }

    /* =========================
     *  Public API
     * ========================= */

    public GumpBuilder page(int page) {
        if (page < 0) throw new IllegalArgumentException("page index must be >= 0");
        currentPage = page;
        line("page " + page);
        return this;
    }

    /* ===== Visuals ===== */

    public GumpBuilder resizePic(int x, int y, int gumpId, int width, int height) {
        requirePage();
        line("resizepic " + x + " " + y + " " + gumpId + " " + width + " " + height);
        return this;
    }

    public GumpBuilder gumpPic(int x, int y, int gumpId) {
        requirePage();
        line("gumppic " + x + " " + y + " " + gumpId);
        return this;
    }

    public GumpBuilder gumpPicTiled(int x, int y, int width, int height, int gumpId) {
        requirePage();
        if (gumpId < 0 || gumpId > 0xFFFF || width <= 0 || height <= 0)
            throw new IllegalArgumentException("Invalid tiled gump image");
        line("gumppictiled " + x + " " + y + " " + width + " " + height + " " + gumpId);
        return this;
    }

    public GumpBuilder checkerTrans(int x, int y, int width, int height) {
        requirePage();
        line("checkertrans " + x + " " + y + " " + width + " " + height);
        return this;
    }

    public GumpBuilder tilePic(int x, int y, int tileId) {
        requirePage();
        line("tilepic  " + x + " " + y + " " + tileId);
        return this;
    }

    public GumpBuilder tilePicHue(int x, int y, int tileId, int hue) {
        requirePage();
        if (hue < 0 || hue > 0xFFFF) throw new IllegalArgumentException("Hue must fit an unsigned short");
        line("tilepichue " + x + " " + y + " " + tileId + " " + hue);
        return this;
    }

    /* ===== Text ===== */

    public GumpBuilder text(int x, int y, int hue, String value) {
        return text(x, y, hue, value, 0xFF);
    }

    /** Optional trailing font is a layout extension; the reference ClassicUO text parser ignores it. */
    public GumpBuilder text(int x, int y, int hue, String value, int font) {
        requirePage();
        if (font < 0 || font > 0xFF) {
            throw new IllegalArgumentException("Label font must fit an unsigned byte");
        }
        int idx = addText(value);
        line("text " + x + " " + y + " " + hue + " " + idx + (font == 0xFF ? "" : " " + font));
        return this;
    }

    public GumpBuilder htmlGump(
            int x,
            int y,
            int width,
            int height,
            String value,
            boolean background,
            boolean scrollbar) {
        requirePage();
        int idx = addText(value);
        line(
                "htmlgump "
                        + x
                        + " "
                        + y
                        + " "
                        + width
                        + " "
                        + height
                        + " "
                        + idx
                        + " "
                        + (background ? 1 : 0)
                        + " "
                        + (scrollbar ? 1 : 0));
        return this;
    }

    /* ===== Interaction ===== */

    public GumpBuilder button(
            int x, int y, int normalId, int pressedId, boolean quit, int page, int buttonId) {
        return button(x, y, normalId, pressedId, quit, page, buttonId, 0);
    }

    /**
     * Emits an optional trailing hover graphic as a layout extension.
     * Unmodified ClassicUO ignores this extra field; it is not part of its standard button command.
     */
    public GumpBuilder button(
            int x, int y, int normalId, int pressedId, boolean quit, int page, int buttonId, int hoverId) {
        requirePage();
        if (hoverId < 0 || hoverId > 0xFFFF) {
            throw new IllegalArgumentException("Button hover graphic must fit an unsigned short");
        }
        line(
                "button "
                        + x
                        + " "
                        + y
                        + " "
                        + normalId
                        + " "
                        + pressedId
                        + " "
                        + (quit ? 1 : 0)
                        + " "
                        + page
                        + " "
                        + buttonId
                        + (hoverId == 0 ? "" : " " + hoverId));
        return this;
    }

    public void pageButton(int x, int y, int normal, int pressed, int page) {
        button(x, y, normal, pressed, false, page, 0);
    }

    public GumpBuilder checkbox(
            int x, int y, int uncheckedId, int checkedId, boolean state, int switchId) {
        requirePage();
        line(
                "checkbox "
                        + x
                        + " "
                        + y
                        + " "
                        + uncheckedId
                        + " "
                        + checkedId
                        + " "
                        + (state ? 1 : 0)
                        + " "
                        + switchId);
        return this;
    }

    public GumpBuilder radio(
            int x, int y, int uncheckedId, int checkedId, boolean state, int switchId) {
        requirePage();
        line(
                "radio "
                        + x
                        + " "
                        + y
                        + " "
                        + uncheckedId
                        + " "
                        + checkedId
                        + " "
                        + (state ? 1 : 0)
                        + " "
                        + switchId);
        return this;
    }

    public GumpBuilder textEntry(
            int x, int y, int width, int height, int hue, int entryId, int textIndex) {
        line(
                "textentry "
                        + x
                        + " "
                        + y
                        + " "
                        + width
                        + " "
                        + height
                        + " "
                        + hue
                        + " "
                        + entryId
                        + " "
                        + textIndex);
        return this;
    }

    /** Adds a cliloc tooltip to the last visual control emitted in the layout. */
    public GumpBuilder tooltip(int clilocId, String... arguments) {
        line(tooltipCommand(clilocId, arguments));
        return this;
    }

    static String tooltipCommand(int clilocId, String... arguments) {
        if (clilocId <= 0) throw new IllegalArgumentException("Tooltip cliloc ID must be positive");
        Objects.requireNonNull(arguments, "Tooltip arguments are required");
        for (String argument : arguments) {
            Objects.requireNonNull(argument, "Tooltip argument is required");
            for (int i = 0; i < argument.length(); i++) {
                char c = argument.charAt(i);
                if (c < 32 || c > 126 || c == '@' || c == '{' || c == '}') {
                    throw new IllegalArgumentException("Tooltip arguments must be printable ASCII without @ or braces");
                }
            }
        }
        // ClassicUO quotes parameters with @ and passes tab-separated arguments to the cliloc translator.
        return "tooltip " + clilocId + (arguments.length == 0 ? "" : " @" + String.join("\t", arguments) + "@");
    }

    /* ===== Flags ===== */

    public GumpBuilder noClose() {
        line("noclose");
        return this;
    }

    public GumpBuilder noDispose() {
        line("nodispose");
        return this;
    }

    public GumpBuilder noMove() {
        line("nomove");
        return this;
    }

    /* =========================
     *  Output
     * ========================= */

    public String buildLayout() {
        return layout.toString();
    }

    public List<String> buildTexts() {
        return List.copyOf(texts);
    }

    /* =========================
     *  Convenience
     * ========================= */

    public BuiltGump build() {
        final var layout = buildLayout();
        final var texts = buildTexts();

        if (log.isDebugEnabled()) {
            log.debug("Gump Layout: {} - Gump Texts: {}", layout, texts);
        }

        return new BuiltGump(layout, texts);
    }

    public static final class BuiltGump {
        public final String layout;
        public final List<String> texts;

        private BuiltGump(String layout, List<String> texts) {
            this.layout = layout;
            this.texts = texts;
        }
    }
}
