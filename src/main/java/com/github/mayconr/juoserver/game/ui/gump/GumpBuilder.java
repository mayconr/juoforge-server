package com.github.mayconr.juoserver.game.ui.gump;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;
import com.github.mayconr.juoserver.network.packet.GumpSelection;


public class GumpBuilder {
    static final int CLILOC_1_NOTHING = 1042971; // ~1_NOTHING~

    private final StringBuilder layout = new StringBuilder();
    private final List<String> texts = new ArrayList<>();
    private int currentPage = -1;
    private final Set<Integer> virtueGraphics = new HashSet<>();

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

    /** Emits the client virtue control with its required positional hue and class parameters. */
    public GumpBuilder virtueGumpItem(int x, int y, int graphicId, int hue) {
        requirePage();
        if (graphicId < 0x69 || graphicId > 0x70) {
            throw new IllegalArgumentException("Virtue graphic must be between 0x69 and 0x70");
        }
        if (hue < 0 || hue > 0xFFFF) throw new IllegalArgumentException("Hue must fit an unsigned short");
        line("gumppic " + x + " " + y + " " + graphicId + " hue=" + hue + " class=virtuegumpitem");
        virtueGraphics.add(graphicId);
        return this;
    }

    public GumpBuilder gumpPicHued(int x, int y, int gumpId, int hue) {
        return coloredGumpPic("gumppichued", x, y, gumpId, hue);
    }

    /** The reference ClassicUO parser recognizes this command but does not enable partial hue. */
    public GumpBuilder gumpPicPartialHued(int x, int y, int gumpId, int hue) {
        return coloredGumpPic("gumppicphued", x, y, gumpId, hue);
    }

    private GumpBuilder coloredGumpPic(String command, int x, int y, int gumpId, int hue) {
        requirePage();
        if (hue < 0 || hue > 0xFFFF) throw new IllegalArgumentException("Hue must fit an unsigned short");
        line(command + " " + x + " " + y + " " + gumpId + " " + hue);
        return this;
    }

    public GumpBuilder picInPic(int x, int y, int gumpId, int sourceX, int sourceY, int width, int height) {
        return regionGumpPic("picinpic", x, y, gumpId, sourceX, sourceY, width, height, null);
    }

    public GumpBuilder picInPicHued(int x, int y, int gumpId, int sourceX, int sourceY,
                                   int width, int height, int hue) {
        return regionGumpPic("picinpichued", x, y, gumpId, sourceX, sourceY, width, height, hue);
    }

    public GumpBuilder picInPicPartialHued(int x, int y, int gumpId, int sourceX, int sourceY,
                                          int width, int height, int hue) {
        return regionGumpPic("picinpicphued", x, y, gumpId, sourceX, sourceY, width, height, hue);
    }

    static void validateImageRegion(int gumpId, int sourceX, int sourceY, int width, int height) {
        if (gumpId < 0 || gumpId > 0xFFFF || sourceX < 0 || sourceX > 0xFFFF
                || sourceY < 0 || sourceY > 0xFFFF || width <= 0 || width > 0xFFFF
                || height <= 0 || height > 0xFFFF) {
            throw new IllegalArgumentException("Image region values must fit unsigned shorts with positive dimensions");
        }
    }

    private GumpBuilder regionGumpPic(String command, int x, int y, int gumpId, int sourceX, int sourceY,
                                      int width, int height, Integer hue) {
        requirePage();
        validateImageRegion(gumpId, sourceX, sourceY, width, height);
        if (hue != null && (hue < 0 || hue > 0xFFFF)) {
            throw new IllegalArgumentException("Hue must fit an unsigned short");
        }
        line(command + " " + x + " " + y + " " + gumpId + " " + sourceX + " " + sourceY
                + " " + width + " " + height + (hue == null ? "" : " " + hue));
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

    public GumpBuilder croppedText(int x, int y, int width, int height, int hue, String value) {
        requirePage();
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Cropped text dimensions must be positive");
        if (hue < 0 || hue > 0xFFFF) throw new IllegalArgumentException("Hue must fit an unsigned short");
        int idx = addText(value);
        line("croppedtext " + x + " " + y + " " + width + " " + height + " " + hue + " " + idx);
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

    public GumpBuilder xmfHtmlGump(int x, int y, int width, int height, int clilocId,
                                   boolean background, boolean scrollbar) {
        requirePage();
        validateClilocHtml(width, height, clilocId);
        line("xmfhtmlgump " + x + " " + y + " " + width + " " + height + " " + clilocId
                + " " + (background ? 1 : 0) + " " + (scrollbar ? 1 : 0));
        return this;
    }

    public GumpBuilder xmfHtmlGumpColor(int x, int y, int width, int height, int clilocId,
                                        boolean background, boolean scrollbar, int color) {
        requirePage();
        validateClilocHtml(width, height, clilocId);
        validateHtmlColor(color);
        line("xmfhtmlgumpcolor " + x + " " + y + " " + width + " " + height + " " + clilocId
                + " " + (background ? 1 : 0) + " " + (scrollbar ? 1 : 0) + " " + color);
        return this;
    }

    public GumpBuilder xmfHtmlTok(int x, int y, int width, int height, int clilocId,
                                  boolean background, boolean scrollbar, int color, String... arguments) {
        requirePage();
        validateClilocHtml(width, height, clilocId);
        validateHtmlColor(color);
        String quoted = clilocArguments(arguments);
        line("xmfhtmltok " + x + " " + y + " " + width + " " + height
                + " " + (background ? 1 : 0) + " " + (scrollbar ? 1 : 0) + " " + color
                + " " + clilocId + quoted);
        return this;
    }

    private static void validateClilocHtml(int width, int height, int clilocId) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Cliloc HTML dimensions must be positive");
        if (clilocId <= 0) throw new IllegalArgumentException("Cliloc ID must be positive");
    }

    static void validateHtmlColor(int color) {
        if (color < 0 || color > 0xFFFF) throw new IllegalArgumentException("HTML color must fit an unsigned short");
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

    /** Item offsets are relative to the button origin; the client uses native button art dimensions. */
    public GumpBuilder buttonTileArt(int x, int y, int normalId, int pressedId, boolean quit,
                                     int page, int buttonId, int itemId, int hue, int itemX, int itemY) {
        requirePage();
        if (normalId < 0 || normalId > 0xFFFF || pressedId < 0 || pressedId > 0xFFFF
                || itemId < 0 || itemId > 0xFFFF || hue < 0 || hue > 0xFFFF) {
            throw new IllegalArgumentException("Button graphics, item ID and hue must fit unsigned shorts");
        }
        line("buttontileart " + x + " " + y + " " + normalId + " " + pressedId + " "
                + (quit ? 1 : 0) + " " + page + " " + buttonId + " " + itemId + " " + hue
                + " " + itemX + " " + itemY);
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

    /** Starts a radio section. ClassicUO uses delimiter order rather than the numeric ID. */
    public GumpBuilder group(int groupId) {
        requirePage();
        if (groupId < 0) throw new IllegalArgumentException("Radio group ID must be nonnegative");
        line("group " + groupId);
        return this;
    }

    /** Ends a radio section; this does not restore a previous group. */
    public GumpBuilder endGroup() {
        requirePage();
        line("endgroup");
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

    /** Emits a client-side character limit compatible with gump responses. */
    public GumpBuilder textEntryLimited(
            int x, int y, int width, int height, int hue, int entryId, int textIndex, int maxCharacters) {
        requirePage();
        if (maxCharacters < 1 || maxCharacters > GumpSelection.MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("Text entry limit must be between 1 and " + GumpSelection.MAX_TEXT_LENGTH);
        }
        line("textentrylimited " + x + " " + y + " " + width + " " + height + " "
                + hue + " " + entryId + " " + textIndex + " " + maxCharacters);
        return this;
    }

    /** Associates the last visual control with an existing object's property list. */
    public GumpBuilder itemProperty(int serialId) {
        requirePage();
        if (serialId <= 0) throw new IllegalArgumentException("Item property serial must be positive");
        line("itemproperty " + serialId);
        return this;
    }

    /** Adds literal text through the client's ~1_NOTHING~ cliloc. */
    public GumpBuilder tooltip(String text) {
        return tooltip(CLILOC_1_NOTHING, text);
    }

    /** Adds a cliloc tooltip to the last visual control emitted in the layout. */
    public GumpBuilder tooltip(int clilocId, String... arguments) {
        line(tooltipCommand(clilocId, arguments));
        return this;
    }

    static String tooltipCommand(int clilocId, String... arguments) {
        if (clilocId <= 0) throw new IllegalArgumentException("Tooltip cliloc ID must be positive");
        return "tooltip " + clilocId + clilocArguments(arguments);
    }

    static String clilocArguments(String... arguments) {
        Objects.requireNonNull(arguments, "Cliloc arguments are required");
        for (String argument : arguments) {
            Objects.requireNonNull(argument, "Cliloc argument is required");
            for (int i = 0; i < argument.length(); i++) {
                char c = argument.charAt(i);
                if (c < 32 || c > 126 || c == '@' || c == '{' || c == '}') {
                    throw new IllegalArgumentException("Cliloc arguments must be printable ASCII without @ or braces");
                }
            }
        }
        // ClassicUO quotes parameters with @ and passes tab-separated arguments to the cliloc translator.
        return arguments.length == 0 ? "" : " @" + String.join("\t", arguments) + "@";
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

        return new BuiltGump(layout, texts, virtueGraphics);
    }

    public static final class BuiltGump {
        public final String layout;
        public final List<String> texts;
        public final Set<Integer> virtueGraphics;

        private BuiltGump(String layout, List<String> texts, Set<Integer> virtueGraphics) {
            this.layout = layout;
            this.texts = texts;
            this.virtueGraphics = Set.copyOf(virtueGraphics);
        }
    }
}
