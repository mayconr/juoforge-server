package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.flow.delete.DeleteItemContext;
import com.github.mayconr.juoserver.game.item.flow.delete.DeleteItemFlowDefinition;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.ItemDeleted;
import com.github.mayconr.juoserver.game.ui.WorldUI;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;
import com.github.mayconr.juoserver.infrastructure.storage.mobile.Mobiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteItemFlowTest {
    private static final int OWNER = 1;
    private static final int BACKPACK = 0x40000001;
    private static final int ORE = 0x40000002;

    private final WorldStorage storage = mock(WorldStorage.class);
    private final Items items = mock(Items.class);
    private final Mobiles mobiles = mock(Mobiles.class);
    private final EventBus events = mock(EventBus.class);
    private final WorldUI ui = mock(WorldUI.class);
    private final Flow<DeleteItemContext> flow = DeleteItemFlowDefinition.build(storage, events, ui);

    @BeforeEach
    void setUp() {
        when(storage.items()).thenReturn(items);
        when(storage.mobiles()).thenReturn(mobiles);
    }

    @Test
    void detachesItemFromEquippedBackpackAndRefreshesOwnerStatus() {
        var backpack = new UOContainer(equippedBackpackData(), null);
        var ore = new UOItem(containedData(ORE, BACKPACK), null);
        backpack.addItemToContainer(ore);
        var owner = mock(UOPlayer.class);
        when(owner.getSerialId()).thenReturn(OWNER);
        when(items.getContainer(BACKPACK)).thenReturn(Optional.of(backpack));
        when(mobiles.get(OWNER)).thenReturn(Optional.of(owner));

        var result = flow.execute(new DeleteItemContext(ore));

        assertTrue(result.flowSucceeded());
        assertTrue(backpack.getContainerItems().isEmpty());
        var order = inOrder(items, events, ui);
        order.verify(items).delete(ore);
        order.verify(events).publish(new ItemDeleted(ore));
        order.verify(ui).sendStatusGump(owner, OWNER);
    }

    @Test
    void deletesItemOutsideContainersWithoutStatusRefresh() {
        var item = new UOItem(data(ORE, ItemLocationType.GROUND, List.of()), null);

        var result = flow.execute(new DeleteItemContext(item));

        assertTrue(result.flowSucceeded());
        verify(items).delete(item);
        verify(events).publish(new ItemDeleted(item));
        verify(items, never()).getContainer(anyInt());
        verifyNoInteractions(ui, mobiles);
    }

    @Test
    void unloadedParentContainerStillDeletesItem() {
        var ore = new UOItem(containedData(ORE, BACKPACK), null);
        when(items.getContainer(BACKPACK)).thenReturn(Optional.empty());

        var result = flow.execute(new DeleteItemContext(ore));

        assertTrue(result.flowSucceeded());
        verify(items).delete(ore);
        verify(events).publish(new ItemDeleted(ore));
        verifyNoInteractions(ui);
    }

    private static UOItemData equippedBackpackData() {
        var data = data(BACKPACK, ItemLocationType.EQUIPPED, List.of(ItemFlag.CONTAINER));
        data.setOwnerSerialId(OWNER);
        return data;
    }

    private static UOItemData containedData(int serial, int containerSerial) {
        var data = data(serial, ItemLocationType.CONTAINER, List.of());
        data.setContainerSerialId(containerSerial);
        return data;
    }

    private static UOItemData data(int serial, ItemLocationType locationType, List<ItemFlag> flags) {
        var data = new UOItemData();
        data.setSerialId(serial);
        data.setLocationType(locationType);
        data.setFlags(flags);
        return data;
    }
}
