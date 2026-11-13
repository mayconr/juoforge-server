package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.datafile.ItemTileInfo;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI.ItemIcon;
import static com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI.Page;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UOItemIconTest {
    @Test
    void sendsTheItemModelAndHue() {
        var layout = render(ItemIcon(item(6585, 2413)));

        assertTrue(layout.contains("{tilepichue 0 0 6585 2413}"), layout);
        assertFalse(layout.contains("tooltip"), layout);
    }

    @Test
    void sendsTheOriginalArtworkWhenTheItemHasNoHue() {
        var layout = render(ItemIcon(item(6585, 0)));

        assertTrue(layout.contains("{tilepic  0 0 6585}"), layout);
    }

    @Test
    void keepsTheItemIconLayoutOptions() {
        var icon = ItemIcon(item(6585, 0), 44, 44).offset(3, 4);

        assertTrue(render(icon).contains("{tilepic  3 4 6585}"));
    }

    @Test
    void reservesFiftyPixelsOnEachAxisByDefault() {
        var icon = ItemIcon(item(6585, 0));

        assertEquals(50, icon.getWidth());
        assertEquals(50, icon.getHeight());
    }

    @Test
    void acceptsACustomLayoutSize() {
        var icon = ItemIcon(item(6585, 0), 44, 30);

        assertEquals(44, icon.getWidth());
        assertEquals(30, icon.getHeight());
    }

    @Test
    void requiresAnItem() {
        assertThrows(NullPointerException.class, () -> new UOItemIcon(null));
    }

    @Test
    void resolvesArtDimensionsThroughTheDsl() {
        var reader = mock(UOFileReader.class);
        var info = new ItemTileInfo(0x1A2C, "lava", false, Set.of(), 0, 0, 0, 0, 0, 0, 44, 65);
        when(reader.getItemTileInfo(0x1A2C)).thenReturn(Optional.of(info));

        var icon = ItemIcon(item(0x1A2C, 2413), reader);

        assertEquals(44, icon.getWidth());
        assertEquals(65, icon.getHeight());
        assertTrue(render(icon).contains("{tilepichue 0 0 6700 2413}"));
    }

    @Test
    void fallsBackWhenTileInfoIsMissing() {
        var reader = mock(UOFileReader.class);
        when(reader.getItemTileInfo(0x1A2C)).thenReturn(Optional.empty());

        var icon = ItemIcon(item(0x1A2C, 0), reader);

        assertEquals(50, icon.getWidth());
        assertEquals(50, icon.getHeight());
    }

    @Test
    void fallsBackOnlyOnTheInvalidAxis() {
        var reader = mock(UOFileReader.class);
        var info = new ItemTileInfo(0x1A2C, "lava", false, Set.of(), 0, 0, 0, 0, 0, 0, 44, 0);
        when(reader.getItemTileInfo(0x1A2C)).thenReturn(Optional.of(info));

        var icon = ItemIcon(item(0x1A2C, 0), reader);

        assertEquals(44, icon.getWidth());
        assertEquals(50, icon.getHeight());
    }

    private static String render(ItemIcon icon) {
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(Page(0, icon)).render(builder);
        return builder.buildLayout();
    }

    private static UOItem item(int modelId, int hue) {
        var item = mock(UOItem.class);
        when(item.getModelId()).thenReturn(modelId);
        when(item.getHue()).thenReturn(hue);
        return item;
    }
}
