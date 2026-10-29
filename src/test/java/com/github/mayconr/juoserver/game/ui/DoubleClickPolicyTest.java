package com.github.mayconr.juoserver.game.ui;

import com.github.mayconr.juoserver.DefaultWorldCfg;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseContext;
import com.github.mayconr.juoserver.game.item.WorldItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.ui.policy.DoubleClickPolicy;
import com.github.mayconr.juoserver.game.ui.policy.DoubleClickRequest;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.DoubleClick;
import com.github.mayconr.juoserver.game.ui.flow.doubleclick.DoubleClickContext;
import com.github.mayconr.juoserver.game.ui.flow.doubleclick.DoubleClickFlowDefinition;
import com.github.mayconr.juoserver.game.ui.flow.paperdoll.OpenPaperdollContext;
import com.github.mayconr.juoserver.game.ui.flow.paperdoll.OpenPaperdollFlowDefinition;
import com.github.mayconr.juoserver.game.world.context.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DoubleClickPolicyTest {
    private final com.github.mayconr.juoserver.game.container.WorldContainer containers =
            mock(com.github.mayconr.juoserver.game.container.WorldContainer.class);
    private UIModule ui(EventBus events, RealmStorage storage, DoubleClickPolicy policy, WorldItem items) {
        var registry = new DefaultFlowRegistry();
        var ui = new UIModuleImpl(null);
        registry.register("double-click", DoubleClickFlowDefinition.build(storage, policy, items, containers), DoubleClickContext.class);
        registry.register("open-paperdoll", OpenPaperdollFlowDefinition.build(events, storage), OpenPaperdollContext.class);
        ui.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));
        return ui;
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void opensPaperdollDirectlyThroughTheUiModule(int serial) {
        var storage = mock(RealmStorage.class);
        var events = mock(EventBus.class);
        var items = mock(WorldItem.class);
        var player = mock(UOPlayer.class);
        var other = mock(com.github.mayconr.juoserver.game.model.UONpc.class);
        when(player.getSerialId()).thenReturn(1);
        when(storage.getMobile(2)).thenReturn(Optional.of(other));
        when(storage.getMobile(3)).thenReturn(Optional.empty());

        ui(events, storage, DoubleClickPolicy.allowAll(), items).openPaperdoll(player, serial);

        if (serial == 3) {
            verifyNoInteractions(events);
        } else {
            verify(events).publish(new com.github.mayconr.juoserver.game.model.event.PaperdollOpened(player,
                    serial == 1 ? player : other, com.github.mayconr.juoserver.network.packet.OpenPaperdoll.Flag.NORMAL));
        }
        verifyNoInteractions(items);
        if (serial == 1) verifyNoInteractions(storage);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void doubleClickOnMobileDoesNotOpenPaperdoll(boolean self) {
        var storage = mock(RealmStorage.class);
        var events = mock(EventBus.class);
        var items = mock(WorldItem.class);
        var player = mock(UOPlayer.class);
        var other = mock(com.github.mayconr.juoserver.game.model.UONpc.class);
        var click = mock(DoubleClick.class);
        when(player.getSerialId()).thenReturn(1);
        when(click.getSerialId()).thenReturn(self ? 1 : 2);
        when(click.isPaperdool()).thenReturn(self);
        when(storage.getMobile(2)).thenReturn(Optional.of(other));
        ui(events, storage, DoubleClickPolicy.allowAll(), items).doubleClick(player, click);
        verifyNoInteractions(events, storage, items);
    }

    @Test
    void delegatesItemUseToTheItemModule() {
        var storage = mock(RealmStorage.class);
        var items = mock(WorldItem.class);
        var events = mock(EventBus.class);
        var player = mock(UOPlayer.class);
        var item = mock(com.github.mayconr.juoserver.game.model.UOItem.class);
        var click = mock(DoubleClick.class);
        when(click.getSerialId()).thenReturn(0x40000000);
        when(storage.getItem(0x40000000)).thenReturn(Optional.of(item));

        ui(events, storage, DoubleClickPolicy.allowAll(), items).doubleClick(player, click);

        verify(items).use(new ItemUseContext(player, item,
                com.github.mayconr.juoserver.game.item.trigger.Trigger.DOUBLE_CLICK));
        verifyNoMoreInteractions(items);
        verifyNoInteractions(events);
    }

    @Test
    void configurationDefaultsToAllowAndAcceptsAShardPolicy() {
        var cfg = new DefaultWorldCfg();
        var request = new DoubleClickRequest(mock(UOPlayer.class), 0x40000000);
        assertTrue(cfg.doubleClickPolicy().get().allows(request));
        DoubleClickPolicy deny = action -> false;
        cfg.doubleClickPolicy(() -> deny);
        assertSame(deny, cfg.doubleClickPolicy().get());
        assertFalse(cfg.doubleClickPolicy().get().allows(request));
        assertThrows(NullPointerException.class, () -> cfg.doubleClickPolicy(null));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void configuredPolicyRunsBeforeStorageAndReceivesThePlayerAndTarget(boolean allowed) {
        var cfg = new DefaultWorldCfg();
        var policy = mock(DoubleClickPolicy.class);
        var storage = mock(RealmStorage.class);
        var events = mock(EventBus.class);

        var items = mock(WorldItem.class);
        var player = mock(UOPlayer.class);
        var click = mock(DoubleClick.class);
        int serial = 0x40000000;
        when(click.getSerialId()).thenReturn(serial);
        when(storage.getItem(serial)).thenReturn(Optional.empty());
        when(policy.allows(new DoubleClickRequest(player, serial))).thenReturn(allowed);
        cfg.doubleClickPolicy(() -> policy);
        var handler = ui(events, storage, cfg.doubleClickPolicy().get(), items);

        handler.doubleClick(player, click);

        if (allowed) {
            var order = inOrder(policy, storage);
            order.verify(policy).allows(new DoubleClickRequest(player, serial));
            order.verify(storage).getItem(serial);
        } else {
            verify(policy).allows(new DoubleClickRequest(player, serial));
            verifyNoInteractions(storage);
        }
        verifyNoInteractions(events, items);
    }

    @Test
    void delegatesContainerOpeningToTheContainerModule() {
        var storage = mock(RealmStorage.class);
        var items = mock(WorldItem.class);
        var events = mock(EventBus.class);

        var player = mock(UOPlayer.class);
        var container = mock(com.github.mayconr.juoserver.game.model.UOContainer.class);
        var click = mock(DoubleClick.class);
        when(click.getSerialId()).thenReturn(0x40000000);
        when(storage.getItem(0x40000000)).thenReturn(Optional.of(container));
        ui(events, storage, DoubleClickPolicy.allowAll(), items).doubleClick(player, click);
        verify(containers).openContainer(player, container);
        verify(storage, never()).loadContainerItems(any());
        verifyNoInteractions(events);
    }
}
