package com.github.mayconr.juoserver.game.ui.gump;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GumpPreviewTest {
    @Test
    void rendersSharedPageAndKeepsOtherPagesSeparate() {
        var builder = new GumpBuilder().page(0).text(0, 0, 0, "Shared")
                .page(1).text(0, 28, 0, "First")
                .page(2).text(0, 28, 0, "Second");

        var preview = GumpPreview.render(builder.build());
        var pages = preview.split("Page 2\n");

        assertTrue(pages[0].contains("Shared\n\nFirst"));
        assertFalse(pages[0].contains("Second"));
        assertEquals("Shared\n\nSecond\n", pages[1]);
    }

    @Test
    void rendersControlsAndConvertsLocalizedHtmlToAscii() {
        var builder = new GumpBuilder().page(0)
                .resizePic(0, 0, 5054, 280, 140)
                .htmlGump(7, 14, 140, 28, "<b>Cria\u00e7\u00e3o</b><br>Item", false, false)
                .radio(7, 56, 208, 209, true, 1)
                .button(70, 56, 247, 248, true, 0, 42);
        builder.textEntry(7, 84, 70, 14, 0, 1, builder.addText("3"));

        var preview = GumpPreview.render(builder.build());

        assertTrue(preview.contains("Criacao"));
        assertTrue(preview.contains("Item"));
        assertTrue(preview.contains("(o)"));
        assertTrue(preview.contains("[B:42]"));
        assertTrue(preview.contains("[3]"));
        assertTrue(preview.contains("+---"));
        assertTrue(preview.chars().allMatch(character -> character == '\n' || character >= 32 && character <= 126));
    }

    @Test
    void clipsOversizedLayoutsAndIgnoresMetadata() {
        var builder = new GumpBuilder().page(0).noClose()
                .resizePic(0, 0, 5054, Integer.MAX_VALUE, Integer.MAX_VALUE)
                .text(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, "Outside");

        var preview = GumpPreview.render(builder.build());

        assertFalse(preview.contains("Outside"));
        assertTrue(preview.lines().count() <= 82);
        assertTrue(preview.lines().allMatch(line -> line.length() <= 140));
    }
}
