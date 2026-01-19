package com.github.mayconr.juoserver.game.gump;

import java.util.ArrayList;
import java.util.Collections;
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

    /* ===== Text ===== */

    public GumpBuilder text(int x, int y, int hue, String value) {
        requirePage();
        int idx = addText(value);
        line("text " + x + " " + y + " " + hue + " " + idx);
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
        requirePage();
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
                        + buttonId);
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
        return Collections.unmodifiableList(texts);
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
