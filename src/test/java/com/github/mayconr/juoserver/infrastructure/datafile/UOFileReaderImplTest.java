package com.github.mayconr.juoserver.infrastructure.datafile;

import com.github.mayconr.juoforge.reader.art.ArtMetadata;
import com.github.mayconr.juoforge.reader.tiledata.ItemTileData;
import com.github.mayconr.juoforge.reader.tiledata.TileFlag;
import com.github.mayconr.juoforge.reader.view.GameDataProvider;
import com.github.mayconr.juoserver.support.TestFields;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UOFileReaderImplTest {

    private static final int MODEL_ID = 0x1A2C;
    private final GameDataProvider provider = mock(GameDataProvider.class);
    private final UOFileReaderImpl reader = new UOFileReaderImpl(null);

    UOFileReaderImplTest() {
        TestFields.setField(reader, "gameDataProvider", provider);
    }

    @Test
    void exposesArtDimensionsInsteadOfTiledataHeight() {
        stubTiledata();
        var art = new ArtMetadata(0x4000 + MODEL_ID, MODEL_ID, false, 0, 0, 0,
                OptionalLong.empty(), 44, 65, List.of());
        when(provider.itemArt(MODEL_ID)).thenReturn(Optional.of(art));

        var info = reader.getItemTileInfo(MODEL_ID).orElseThrow();

        assertEquals(44, info.width());
        assertEquals(65, info.height());
        assertEquals("lava", info.name());
        assertTrue(info.flags().contains("DAMAGING"));
    }

    @Test
    void keepsTiledataWhenArtIsMissing() {
        stubTiledata();
        when(provider.itemArt(MODEL_ID)).thenReturn(Optional.empty());

        var info = reader.getItemTileInfo(MODEL_ID).orElseThrow();

        assertEquals("lava", info.name());
        assertEquals(0, info.width());
        assertEquals(0, info.height());
    }

    @Test
    void returnsEmptyWhenTiledataIsMissing() {
        when(provider.itemTileData(MODEL_ID)).thenReturn(Optional.empty());

        assertTrue(reader.getItemTileInfo(MODEL_ID).isEmpty());
    }

    private void stubTiledata() {
        var tile = new ItemTileData(Set.of(TileFlag.DAMAGING), 0, 0, 0, 0, 0, 0, 10, "lava");
        when(provider.itemTileData(MODEL_ID)).thenReturn(Optional.of(tile));
    }
}
