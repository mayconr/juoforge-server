package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.ui.gump.GumpBuilder.BuiltGump;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;

/** Approximate ASCII rendering of a built layout; no client artwork is loaded. */
public final class GumpPreview {
    private static final int COLUMNS = 140;
    private static final int ROWS = 80;
    private static final int CELL_WIDTH = 7;
    private static final int CELL_HEIGHT = 14;
    private static final Pattern COMMAND = Pattern.compile("\\{([^{}]*)}");

    private GumpPreview() {}

    public static String render(BuiltGump gump) {
        var commands = parse(gump.layout);
        var pages = new TreeSet<Integer>();
        commands.stream().map(Command::page).filter(page -> page > 0).forEach(pages::add);
        if (pages.isEmpty()) {
            pages.add(0);
        }
        var output = new StringBuilder("ASCII preview (7x14 pixels/cell; max 140x80 cells/page)\n");
        for (int page : pages) {
            output.append("Page ").append(page).append('\n');
            output.append(renderPage(commands, gump.texts, page));
        }
        return output.toString();
    }

    private static List<Command> parse(String layout) {
        var commands = new ArrayList<Command>();
        var matcher = COMMAND.matcher(layout);
        int page = 0;
        while (matcher.find()) {
            var fields = matcher.group(1).trim().split("\\s+");
            if (fields[0].equals("page")) {
                page = number(fields, 1);
            } else {
                commands.add(new Command(page, fields));
            }
        }
        return commands;
    }

    private static String renderPage(List<Command> commands, List<String> texts, int page) {
        var canvas = new Canvas();
        for (var command : commands) {
            if (command.page() == 0 || command.page() == page) {
                draw(canvas, command.fields(), texts);
            }
        }
        return canvas.render();
    }

    private static void draw(Canvas canvas, String[] fields, List<String> texts) {
        int x = Math.floorDiv(number(fields, 1), CELL_WIDTH);
        int y = Math.floorDiv(number(fields, 2), CELL_HEIGHT);
        switch (fields[0]) {
            case "resizepic" -> canvas.box(x, y, cells(fields, 4, CELL_WIDTH), cells(fields, 5, CELL_HEIGHT));
            case "gumppictiled", "checkertrans" ->
                    canvas.box(x, y, cells(fields, 3, CELL_WIDTH), cells(fields, 4, CELL_HEIGHT));
            case "text" -> canvas.text(x, y, text(texts, fields, 4), COLUMNS);
            case "croppedtext", "htmlgump" -> canvas.text(
                    x, y, text(texts, fields, fields[0].equals("htmlgump") ? 5 : 6),
                    cells(fields, 3, CELL_WIDTH)
            );
            case "textentry", "textentrylimited" -> canvas.text(
                    x, y, "[" + text(texts, fields, 7) + "]", cells(fields, 3, CELL_WIDTH)
            );
            case "button", "buttontileart" -> canvas.text(x, y, "[B:" + number(fields, 7) + "]", COLUMNS);
            case "checkbox" -> canvas.text(x, y, number(fields, 5) == 0 ? "[ ]" : "[x]", 3);
            case "radio" -> canvas.text(x, y, number(fields, 5) == 0 ? "( )" : "(o)", 3);
            case "tilepic", "tilepichue" -> canvas.text(x, y, "[item:" + number(fields, 3) + "]", COLUMNS);
            case "gumppic", "gumppichued", "gumppicphued", "picinpic", "picinpichued" ->
                    canvas.text(x, y, "[img:" + number(fields, 3) + "]", COLUMNS);
            case "xmfhtmlgump", "xmfhtmlgumpcolor", "xmfhtmltok" -> canvas.text(
                    x, y, "[cliloc:" + number(fields, fields[0].equals("xmfhtmltok") ? 8 : 5) + "]",
                    cells(fields, 3, CELL_WIDTH)
            );
            default -> { /* Metadata has no visible representation. */ }
        }
    }

    private static int cells(String[] fields, int index, int scale) {
        return (int) Math.max(1, Math.min(COLUMNS, ((long) number(fields, index) + scale - 1) / scale));
    }

    private static int number(String[] fields, int index) {
        if (index >= fields.length) {
            return 0;
        }
        try {
            return Integer.parseInt(fields[index]);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static String text(List<String> texts, String[] fields, int index) {
        int id = number(fields, index);
        return id >= 0 && id < texts.size() ? texts.get(id) : "";
    }

    private static String ascii(String text) {
        var plain = text.replaceAll("(?i)<br\\s*/?>", "\n").replaceAll("<[^>]*>", "")
                .replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">")
                .replace("&amp;", "&");
        return Normalizer.normalize(plain, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replaceAll("[^\\x20-\\x7E\\n]", "?");
    }

    private record Command(int page, String[] fields) {}

    private static final class Canvas {
        private final char[][] pixels = new char[ROWS][COLUMNS];
        private int width;
        private int height;

        private Canvas() {
            for (var row : pixels) {
                Arrays.fill(row, ' ');
            }
        }

        private void put(int x, int y, char value) {
            if (x < 0 || y < 0 || x >= COLUMNS || y >= ROWS) {
                return;
            }
            pixels[y][x] = value;
            width = Math.max(width, x + 1);
            height = Math.max(height, y + 1);
        }

        private void box(int x, int y, int width, int height) {
            for (int column = 0; column < width; column++) {
                put(x + column, y, '-');
                put(x + column, y + height - 1, '-');
            }
            for (int row = 0; row < height; row++) {
                put(x, y + row, '|');
                put(x + width - 1, y + row, '|');
            }
            put(x, y, '+');
            put(x + width - 1, y, '+');
            put(x, y + height - 1, '+');
            put(x + width - 1, y + height - 1, '+');
        }

        private void text(int x, int y, String value, int limit) {
            int column = 0;
            int row = 0;
            for (char character : ascii(value).toCharArray()) {
                if (character == '\n') {
                    column = 0;
                    row++;
                } else {
                    if (column < limit) {
                        put(x + column, y + row, character);
                    }
                    column++;
                }
            }
        }

        private String render() {
            var output = new StringBuilder();
            for (int row = 0; row < height; row++) {
                output.append(new String(pixels[row], 0, width).stripTrailing()).append('\n');
            }
            return output.toString();
        }
    }
}
