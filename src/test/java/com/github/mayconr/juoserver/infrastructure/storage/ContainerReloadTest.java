package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContainerReloadTest {
    private UOItemData data(int serial, int container, int amount) {
        var data = new UOItemData();
        data.setSerialId(serial);
        data.setFlags(List.of(ItemFlag.STACKABLE));
        data.setLocationType(ItemLocationType.CONTAINER);
        data.setContainerSerialId(container);
        data.setAmount(amount);
        return data;
    }

    @Test
    void openingBackpackDoesNotRestoreItemsMovedToCorpseBeforeSave() {
        var items = mock(ItemStorage.class);
        var mapper = new ItemMapper(new InMemoryTemplateRegistry<String, ItemTemplate>(List.of(), ItemTemplate::name));
        var storage = new CachedRealmStorage(null, items, null, mapper);
        var backpack = mock(UOContainer.class);
        when(backpack.getSerialId()).thenReturn(100);
        var current = new UOItem(data(101, 200, 10), null);
        storage.cache(current);
        when(items.loadContainerItems(100)).thenReturn(CompletableFuture.completedFuture(List.of(data(101, 100, 100))));
        assertTrue(storage.loadContainerItems(backpack).join().isEmpty());
        assertSame(current, storage.getItem(101).orElseThrow());
        assertEquals(new ContainerLocation(200), current.getCurrentLocation());
    }

    @Test
    void loadingExistingContainerKeepsTheCurrentCachedInstancesAndAmounts() {
        var items = mock(ItemStorage.class);
        var mapper = new ItemMapper(new InMemoryTemplateRegistry<String, ItemTemplate>(List.of(), ItemTemplate::name));
        var storage = new CachedRealmStorage(null, items, null, mapper);
        var backpack = mock(UOContainer.class);
        when(backpack.getSerialId()).thenReturn(100);
        var current = new UOItem(data(101, 100, 10), null);
        storage.cache(current);
        when(items.loadContainerItems(100)).thenReturn(CompletableFuture.completedFuture(List.of(data(101, 100, 100))));
        var loaded = storage.loadContainerItems(backpack).join();
        assertEquals(1, loaded.size());
        assertSame(current, loaded.getFirst());
        assertEquals(10, loaded.getFirst().getAmount());
    }
}
