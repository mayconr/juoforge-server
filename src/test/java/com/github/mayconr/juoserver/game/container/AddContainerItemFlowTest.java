package com.github.mayconr.juoserver.game.container;

import com.github.mayconr.juoserver.game.container.flow.add.AddContainerItemContext;
import com.github.mayconr.juoserver.game.container.flow.add.AddContainerItemFlowDefinition;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.ItemCreatedInContainer;
import com.github.mayconr.juoserver.game.model.event.ItemInventoryChanged;
import com.github.mayconr.juoserver.game.ui.WorldUI;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.infrastructure.storage.mobile.Mobiles;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddContainerItemFlowTest {
    @ParameterizedTest
    @CsvSource({"true, true", "true, false", "false, true"})
    void refreshesPlayerStatusAfterPlacementWhenNotificationsEnabled(boolean playerOwner, boolean notifyClient) {
        var storage = mock(WorldStorage.class);
        var mobiles = mock(Mobiles.class);
        var events = mock(EventBus.class);
        var ui = mock(WorldUI.class);
        var container = mock(UOContainer.class);
        var item = mock(UOItem.class);
        UOMobile owner = playerOwner ? mock(UOPlayer.class) : mock(UONpc.class);
        when(owner.getSerialId()).thenReturn(42);
        when(container.getCurrentLocation()).thenReturn(ItemLocation.equipped(42));
        when(storage.mobiles()).thenReturn(mobiles);
        when(mobiles.get(42)).thenReturn(Optional.of(owner));
        if (playerOwner && notifyClient) {
            doAnswer(invocation -> {
                verify(container).addItemToContainer(item);
                return null;
            }).when(ui).sendStatusGump((UOPlayer) owner, 42);
        }
        var context = new AddContainerItemContext(container, item, false, notifyClient);
        var result = AddContainerItemFlowDefinition.build(storage, events, ui).execute(context);
        assertTrue(result.flowSucceeded());
        assertSame(item, context.result());
        if (playerOwner && notifyClient) verify(ui).sendStatusGump((UOPlayer) owner, 42);
        else verifyNoInteractions(ui);
        verify(events).publish(new ItemInventoryChanged(item, null, ItemLocation.container(container.getSerialId())));
        if (!notifyClient) verify(events, never()).publish(isA(ItemCreatedInContainer.class));
    }
}
