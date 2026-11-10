package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GumpItemButtonTest {
    @Test
    void emitsNativeCommandWithDefaultHueOffsetsAndReplyId() {
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(new Page(0).add(DeclarativeGumpUI.ItemButton(4005, 4007, 42, 0x0FBB)))
                .render(builder);
        assertEquals("{page 0}\n{buttontileart 0 0 4005 4007 1 0 42 4027 0 0 0}\n", builder.buildLayout());
    }

    @Test
    void preservesItemOffsetsHueCaptionLocalizationAndTooltip() {
        var button = new ItemButton(4005, 4007, 42, 4027, MessageContent.localized("craft"))
                .hue(1152).itemOffset(-2, 7).captionCentered().tooltip(1042971);
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(new Page(0).add(new Panel(3000, 200, 100, button)))
                .render(builder, content -> "Craft");
        assertTrue(builder.buildLayout().contains("{buttontileart 10 10 4005 4007 1 0 42 4027 1152 -2 7}\n{tooltip 1042971}"));
        assertEquals(List.of("Craft"), builder.buildTexts());
        assertTrue(builder.buildLayout().contains("{text 32 14 1152 0}"));
    }

    @Test
    void builderSupportsPageActionAndRequiresPage() {
        var builder = new GumpBuilder();
        assertThrows(IllegalStateException.class,
                () -> builder.buttonTileArt(1, 2, 4005, 4007, true, 0, 42, 4027, 0, 0, 0));
        builder.page(0).buttonTileArt(1, 2, 4005, 4007, false, 3, 42, 4027, 100, 4, 5);
        assertTrue(builder.buildLayout().contains("{buttontileart 1 2 4005 4007 0 3 42 4027 100 4 5}"));
    }

    @Test
    void validatesGraphicIdsAndHue() {
        for (int invalid : new int[]{-1, 65536}) {
            assertThrows(IllegalArgumentException.class, () -> new ItemButton(invalid, 4007, 42, 4027));
            assertThrows(IllegalArgumentException.class, () -> new ItemButton(4005, invalid, 42, 4027));
            assertThrows(IllegalArgumentException.class, () -> new ItemButton(4005, 4007, 42, invalid));
            assertThrows(IllegalArgumentException.class, () -> new ItemButton(4005, 4007, 42, 4027).hue(invalid));
            assertThrows(IllegalArgumentException.class, () -> new GumpBuilder().page(0)
                    .buttonTileArt(0, 0, 4005, 4007, true, 0, 42, 4027, invalid, 0, 0));
        }
    }
}
