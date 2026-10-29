package com.github.mayconr.juoserver.game.container;

import com.github.mayconr.juoserver.game.container.flow.add.*;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.ui.WorldUI;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddContainerItemFlowTest {
    @ParameterizedTest
    @CsvSource({"true, true", "true, false", "false, true"})
    void refreshesPlayerStatusAfterPlacementWhenNotificationsEnabled(boolean playerOwner, boolean notifyClient) {
        var storage = mock(RealmStorage.class);
        var events = mock(EventBus.class);
        var ui = mock(WorldUI.class);
        var container = mock(UOContainer.class);
        var item = mock(UOItem.class);
        UOMobile owner = playerOwner ? mock(UOPlayer.class) : mock(UONpc.class);
        when(owner.getSerialId()).thenReturn(42);
        when(container.getCurrentLocation()).thenReturn(ItemLocation.equipped(42));
        when(storage.getMobile(42)).thenReturn(Optional.of(owner));
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
        if (!notifyClient) verifyNoInteractions(events);
    }
}
