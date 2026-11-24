package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.infrastructure.storage.item.ItemMapper;
import com.github.mayconr.juoserver.infrastructure.storage.item.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.mobile.MobileStorage;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContainerReloadTest {
    @Test
    void loadingPlayerWaitsForBackpackContentsAndIndexesThem() {
        int serial = 1;
        int backpackSerial = 0x40000001;
        int coinSerial = 0x40000002;
        var mobiles = mock(MobileStorage.class);
        var items = mock(ItemStorage.class);
        var mapper = new ItemMapper(new InMemoryTemplateRegistry<String, ItemTemplate>(List.of(), ItemTemplate::name));
        var storage = new CachedWorldStorage(mobiles, items, null, mapper);
        var playerData = new UOMobileData();
        playerData.setSerialId(serial);
        playerData.setType("P");
        var backpackData = new UOItemData();
        backpackData.setSerialId(backpackSerial);
        backpackData.setLayer(Layer.BACKPACK);
        backpackData.setFlags(List.of(ItemFlag.CONTAINER));
        backpackData.setLocationType(ItemLocationType.EQUIPPED);
        backpackData.setOwnerSerialId(serial);
        var contents = new CompletableFuture<List<UOItemData>>();
        when(mobiles.findMobileBySerialId(serial)).thenReturn(CompletableFuture.completedFuture(playerData));
        when(mobiles.findSkillsBySerialId(serial)).thenReturn(CompletableFuture.completedFuture(List.of()));
        when(mobiles.findAllNpcs()).thenReturn(CompletableFuture.completedFuture(List.of()));
        when(items.findAllGroundItems()).thenReturn(CompletableFuture.completedFuture(List.of()));
        when(items.findAllEquippedItems(serial)).thenReturn(CompletableFuture.completedFuture(List.of(backpackData)));
        when(items.loadContainerItems(backpackSerial)).thenReturn(contents);
        storage.initialize(() -> coinSerial, () -> serial, initialData -> { });

        var loadingPlayer = storage.mobiles().load(serial);
        assertFalse(loadingPlayer.isDone());
        contents.complete(List.of(data(coinSerial, backpackSerial, 20)));

        var player = loadingPlayer.join();
        var backpack = storage.items().getContainer(player.getBackpack()).orElseThrow();
        assertTrue(backpack.getContainerItems().contains(coinSerial));
        assertEquals(20, storage.items().get(coinSerial).orElseThrow().getAmount());
    }

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
        var storage = new CachedWorldStorage(null, items, null, mapper);
        var backpack = mock(UOContainer.class);
        when(backpack.getSerialId()).thenReturn(100);
        var current = new UOItem(data(101, 200, 10), null);
        storage.items().cache(current);
        when(items.loadContainerItems(100)).thenReturn(CompletableFuture.completedFuture(List.of(data(101, 100, 100))));
        assertTrue(storage.items().loadContainerItems(backpack).join().isEmpty());
        assertSame(current, storage.items().get(101).orElseThrow());
        assertEquals(new ContainerLocation(200), current.getCurrentLocation());
    }

    @Test
    void loadingExistingContainerKeepsTheCurrentCachedInstancesAndAmounts() {
        var items = mock(ItemStorage.class);
        var mapper = new ItemMapper(new InMemoryTemplateRegistry<String, ItemTemplate>(List.of(), ItemTemplate::name));
        var storage = new CachedWorldStorage(null, items, null, mapper);
        var backpack = mock(UOContainer.class);
        when(backpack.getSerialId()).thenReturn(100);
        var current = new UOItem(data(101, 100, 10), null);
        storage.items().cache(current);
        when(items.loadContainerItems(100)).thenReturn(CompletableFuture.completedFuture(List.of(data(101, 100, 100))));
        var loaded = storage.items().loadContainerItems(backpack).join();
        assertEquals(1, loaded.size());
        assertSame(current, loaded.getFirst());
        assertEquals(10, loaded.getFirst().getAmount());
    }
}
