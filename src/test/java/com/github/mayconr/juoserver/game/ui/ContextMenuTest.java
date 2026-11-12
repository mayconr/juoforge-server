package com.github.mayconr.juoserver.game.ui;

import com.github.mayconr.juoserver.DefaultWorldConfigurator;
import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.model.DefaultAttributeMap;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.ContextMenuRequested;
import com.github.mayconr.juoserver.game.ui.contextmenu.ContextMenuEntry;
import com.github.mayconr.juoserver.game.ui.contextmenu.ItemContextMenuContext;
import com.github.mayconr.juoserver.game.ui.contextmenu.MobileContextMenuContext;
import com.github.mayconr.juoserver.game.ui.gump.GumpSystem;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ContextMenuTest {
    @Test
    void filtersEntriesUsingCurrentItemLocation() {
        var storage = mock(WorldStorage.class, RETURNS_DEEP_STUBS);
        var events = mock(EventBus.class);
        var item = mock(UOItem.class);
        var player = mock(UOPlayer.class);
        when(player.runtimeAttributes()).thenReturn(new DefaultAttributeMap());
        var inside = new ContextMenuEntry(3060001, context -> context instanceof ItemContextMenuContext itemContext
                && itemContext.player() == player && itemContext.target().isInContainer());
        var outside = new ContextMenuEntry(3060002, context -> context instanceof ItemContextMenuContext itemContext
                && !itemContext.target().isInContainer());
        var ui = new UIModuleImpl(mock(GumpSystem.class), storage, events, List.of(inside, outside));
        when(storage.items().get(0x40000001)).thenReturn(Optional.of(item));
        when(item.getSerialId()).thenReturn(0x40000001);
        when(item.isInContainer()).thenReturn(true);

        ui.requestContextMenu(player, 0x40000001);
        when(item.isInContainer()).thenReturn(false);
        ui.requestContextMenu(player, 0x40000001);

        verify(events).publish(new ContextMenuRequested(player, 0x40000001, List.of(new ContextMenuRequested.Entry(1, inside.cliloc()))));
        verify(events).publish(new ContextMenuRequested(player, 0x40000001, List.of(new ContextMenuRequested.Entry(2, outside.cliloc()))));
    }

    @Test
    void publishesEmptyMenuWhenNoEntryMatches() {
        var storage = mock(WorldStorage.class, RETURNS_DEEP_STUBS);
        var events = mock(EventBus.class);
        var player = mock(UOPlayer.class);
        when(player.runtimeAttributes()).thenReturn(new DefaultAttributeMap());
        var item = mock(UOItem.class);
        when(item.getSerialId()).thenReturn(0x40000001);
        when(storage.items().get(0x40000001)).thenReturn(Optional.of(item));
        var ui = new UIModuleImpl(mock(GumpSystem.class), storage, events,
                List.of(new ContextMenuEntry(3060001, context -> false)));

        ui.requestContextMenu(player, 0x40000001);

        verify(events).publish(new ContextMenuRequested(player, 0x40000001, List.of()));
    }

    @Test
    void ignoresMissingPlayerAndUnknownItem() {
        var storage = mock(WorldStorage.class, RETURNS_DEEP_STUBS);
        var events = mock(EventBus.class);
        when(storage.items().get(0x40000001)).thenReturn(Optional.empty());
        when(storage.mobiles().get(42)).thenReturn(Optional.empty());
        var ui = new UIModuleImpl(mock(GumpSystem.class), storage, events, List.of());

        ui.requestContextMenu(null, 0x40000001);
        var player = mock(UOPlayer.class);
        when(player.runtimeAttributes()).thenReturn(new DefaultAttributeMap());
        ui.requestContextMenu(player, 0x40000001);
        ui.requestContextMenu(player, 42);

        verifyNoInteractions(events);
    }

    @Test
    void filtersMobileMenuUsingPlayerAndTypedTarget() {
        var storage = mock(WorldStorage.class, RETURNS_DEEP_STUBS);
        var events = mock(EventBus.class);
        var player = mock(UOPlayer.class);
        when(player.runtimeAttributes()).thenReturn(new DefaultAttributeMap());
        var mobile = mock(UOMobile.class);
        when(mobile.getSerialId()).thenReturn(42);
        when(mobile.isAlive()).thenReturn(true);
        when(storage.mobiles().get(42)).thenReturn(Optional.of(mobile));
        var mobileEntry = new ContextMenuEntry(3060001,
                context -> context instanceof MobileContextMenuContext mobileContext
                        && mobileContext.player() == player
                        && mobileContext.target() == mobile
                        && mobileContext.target().isAlive());
        var itemEntry = new ContextMenuEntry(3060002,
                context -> context instanceof ItemContextMenuContext);
        var ui = new UIModuleImpl(mock(GumpSystem.class), storage, events, List.of(mobileEntry, itemEntry));

        ui.requestContextMenu(player, 42);

        verify(events).publish(new ContextMenuRequested(player, 42, List.of(new ContextMenuRequested.Entry(1, mobileEntry.cliloc()))));
        verifyNoInteractions(storage.items());
    }

    @Test
    void registryAcceptsIndependentDefinitionsWithTheSameCliloc() {
        var configuration = new DefaultWorldConfigurator();
        var entry = new ContextMenuEntry(3060001, context -> true);
        configuration.addContextMenuEntry(runtime -> entry);
        var ui = new UIModuleImpl(mock(GumpSystem.class), mock(WorldStorage.class), mock(EventBus.class), List.of());
        var runtime = mock(ServerRuntime.class);
        var entries = configuration.contextMenuEntries().stream().map(factory -> factory.apply(runtime)).toList();

        assertEquals(List.of(entry), entries);
        ui.registerContextMenuEntries(entries);
        ui.registerContextMenuEntries(List.of(entry, entry));
        assertThrows(UnsupportedOperationException.class, () -> configuration.contextMenuEntries().clear());
    }
}
