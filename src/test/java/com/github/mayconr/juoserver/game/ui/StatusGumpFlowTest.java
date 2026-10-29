package com.github.mayconr.juoserver.game.ui;

import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.StatusGumpRequested;
import com.github.mayconr.juoserver.game.ui.flow.status.StatusGumpContext;
import com.github.mayconr.juoserver.game.ui.flow.status.StatusGumpFlowDefinition;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.world.context.*;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatusGumpFlowTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void requestsStatusOfTheTargetOrReportsMissingMobile(boolean exists) {
        var storage = mock(RealmStorage.class);
        var events = mock(EventBus.class);
        var player = mock(UOPlayer.class);
        var target = mock(UONpc.class);
        when(target.getBackpack()).thenReturn(null);
        when(storage.getMobile(42)).thenReturn(exists ? Optional.of(target) : Optional.empty());
        var registry = new DefaultFlowRegistry();
        registry.register("status", StatusGumpFlowDefinition.build(events, storage, new com.github.mayconr.juoserver.game.container.ContainerModuleImpl(storage, new com.github.mayconr.juoserver.game.item.ContainerHandler(events, storage))), StatusGumpContext.class);
        UIModule ui = new UIModuleImpl(null);
        ui.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));

        if (exists) {
            ui.sendStatusGump(player, 42);
            verify(events).publish(new StatusGumpRequested(player, target, 0));
        } else {
            var error = assertThrows(ModuleOperationException.class, () -> ui.sendStatusGump(player, 42));
            assertEquals("MOBILE_NOT_FOUND", error.getCode());
            verifyNoInteractions(events);
        }
        verify(storage).getMobile(42);
    }
    @org.junit.jupiter.api.Test
    void includesBackpackWeightFromContainerModule() {
        var storage = mock(RealmStorage.class);
        var events = mock(EventBus.class);
        var player = mock(UOPlayer.class);
        var containers = mock(com.github.mayconr.juoserver.game.container.WorldContainer.class);
        var backpack = mock(com.github.mayconr.juoserver.game.model.UOContainer.class);
        when(storage.getMobile(42)).thenReturn(Optional.of(player));
        when(player.getBackpack()).thenReturn(100);
        when(storage.getContainer(100)).thenReturn(Optional.of(backpack));
        when(containers.getWeight(backpack)).thenReturn(27L);
        var result = StatusGumpFlowDefinition.build(events, storage, containers)
                .execute(new StatusGumpContext(player, 42));
        assertTrue(result.flowSucceeded());
        verify(events).publish(new StatusGumpRequested(player, player, 27));
    }

    @org.junit.jupiter.api.Test
    void missingBackpackDoesNotSendIncorrectWeight() {
        var storage = mock(RealmStorage.class);
        var events = mock(EventBus.class);
        var player = mock(UOPlayer.class);
        when(storage.getMobile(42)).thenReturn(Optional.of(player));
        when(player.getBackpack()).thenReturn(100);
        var result = StatusGumpFlowDefinition.build(events, storage,
                mock(com.github.mayconr.juoserver.game.container.WorldContainer.class))
                .execute(new StatusGumpContext(player, 42));
        assertEquals("BACKPACK_NOT_FOUND", result.code());
        verifyNoInteractions(events);
    }
}
