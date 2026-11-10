package com.github.mayconr.juoserver.game.ui.gump;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GumpImageRegionTest {
    private String render(Image image) {
        image.layout(new LayoutContext(10, 20, 100, 100));
        var builder = new GumpBuilder().page(0);
        image.render(builder);
        return builder.buildLayout();
    }

    @Test
    void emitsAllRegionCommandsAndKeepsSourceSeparateFromDestination() {
        var image = new Image(111, 100, 100).cropped(5, 7, 30, 40);
        assertTrue(render(image).contains("{picinpic 10 20 111 5 7 30 40}"));
        image.hue(100);
        assertTrue(render(image).contains("{picinpichued 10 20 111 5 7 30 40 100}"));
        image.partialHue(200);
        assertTrue(render(image).contains("{picinpicphued 10 20 111 5 7 30 40 200}"));
        image.hue(300);
        assertTrue(render(image).contains("{picinpichued 10 20 111 5 7 30 40 300}"));
    }

    @Test
    void usesRegionDimensionsForCenteringAndPreservesItemProperties() {
        var image = new Image(111, 100, 100).partialHue(100).cropped(5, 7, 30, 40);
        image.itemProperty(123);
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(new Page(0).add(new Panel(3000, 200, 200, image).center())).render(builder);
        assertEquals(30, image.getWidth());
        assertEquals(40, image.getHeight());
        assertTrue(builder.buildLayout().contains("{picinpicphued 85 80 111 5 7 30 40 100}\n{itemproperty 123}"));
        image.tooltip("Region");
        assertTrue(render(image).contains("{picinpicphued 10 20 111 5 7 30 40 100}\n{tooltip 1042971 @Region@}"));
    }

    @Test
    void validatesSourceCoordinatesDimensionsAndHue() {
        var image = new Image(111, 100, 100);
        assertThrows(IllegalArgumentException.class, () -> image.cropped(-1, 0, 10, 10));
        assertThrows(IllegalArgumentException.class, () -> image.cropped(0, 65536, 10, 10));
        assertThrows(IllegalArgumentException.class, () -> image.cropped(0, 0, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> image.cropped(0, 0, 10, 65536));
        var builder = new GumpBuilder().page(0);
        assertThrows(IllegalArgumentException.class, () -> builder.picInPic(0, 0, 111, 0, -1, 10, 10));
        assertThrows(IllegalArgumentException.class, () -> builder.picInPicHued(0, 0, 111, 0, 0, 10, 10, -1));
        assertThrows(IllegalArgumentException.class, () -> builder.picInPicPartialHued(0, 0, 111, 0, 0, 10, 10, 65536));
        assertEquals("{page 0}\n", builder.buildLayout());
        assertEquals(100, image.getWidth());
    }

    @Test
    void requiresActivePageForAllCommands() {
        var builder = new GumpBuilder();
        assertThrows(IllegalStateException.class, () -> builder.picInPic(0, 0, 111, 0, 0, 10, 10));
        assertThrows(IllegalStateException.class, () -> builder.picInPicHued(0, 0, 111, 0, 0, 10, 10, 100));
        assertThrows(IllegalStateException.class, () -> builder.picInPicPartialHued(0, 0, 111, 0, 0, 10, 10, 100));
    }
}
