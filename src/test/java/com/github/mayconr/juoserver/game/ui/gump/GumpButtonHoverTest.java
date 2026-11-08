package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI.Button;
import static com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI.Page;

class GumpButtonHoverTest {
    @Test
    void preservesTheStandardLayoutWhenHoverIsNotConfigured() {
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(Page(0, Button(4005, 4007, 101))).render(builder);
        assertTrue(builder.buildLayout().contains("{button 0 0 4005 4007 1 0 101}"));
    }

    @Test
    void appendsTheHoverExtensionWithoutChangingButtonOrCaptionIds() {
        var ui = new DeclarativeGumpUI(Page(0,
                Button(4005, 4007, 101, MessageContent.localized("category.weapons"))
                        .hoverId(4006).tooltip(1042971, "help")));
        var builder = new GumpBuilder();
        ui.render(builder, content -> "Weapons");
        assertTrue(builder.buildLayout().contains("{button 0 0 4005 4007 1 0 101 4006}"));
        assertEquals("Weapons", builder.buildTexts().getFirst());
        assertTrue(builder.buildLayout().contains("{tooltip 1042971 @help@}"));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 65536})
    void rejectsInvalidGraphicIds(int id) {
        assertThrows(IllegalArgumentException.class, () -> Button(4005, 4007, 101).hoverId(id));
        assertThrows(IllegalArgumentException.class,
                () -> new GumpBuilder().page(0).button(0, 0, 4005, 4007, true, 0, 101, id));
    }

    @Test
    void zeroDisablesTheHoverExtension() {
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(Page(0, Button(4005, 4007, 101).hoverId(4006).hoverId(0))).render(builder);
        assertTrue(builder.buildLayout().contains("{button 0 0 4005 4007 1 0 101}"));
    }
}
