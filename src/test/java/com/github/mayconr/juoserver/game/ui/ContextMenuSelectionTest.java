package com.github.mayconr.juoserver.game.ui;

import com.github.mayconr.juoserver.game.model.DefaultAttributeMap;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.ContextMenuRequested;
import com.github.mayconr.juoserver.game.ui.contextmenu.ContextMenuContext;
import com.github.mayconr.juoserver.game.ui.contextmenu.ContextMenuEntry;
import com.github.mayconr.juoserver.game.ui.contextmenu.ItemContextMenuContext;
import com.github.mayconr.juoserver.game.ui.contextmenu.MobileContextMenuContext;
import com.github.mayconr.juoserver.game.ui.gump.GumpSystem;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ContextMenuSelectionTest {
    private static final int ITEM_SERIAL = 0x40000001;
    private final WorldStorage storage = mock(WorldStorage.class, RETURNS_DEEP_STUBS);
    private final UOPlayer player = mock(UOPlayer.class);
    private final UOItem item = mock(UOItem.class);
    private final Consumer<ContextMenuContext> callback = mock();
    private final EventBus events = mock(EventBus.class);
    private final AtomicLong clock = new AtomicLong();

    private UIModuleImpl ui(ContextMenuEntry... entries) {
        when(player.runtimeAttributes()).thenReturn(new DefaultAttributeMap());
        when(item.getSerialId()).thenReturn(ITEM_SERIAL);
        when(storage.items().get(ITEM_SERIAL)).thenReturn(Optional.of(item));
        return new UIModuleImpl(mock(GumpSystem.class), storage, events, List.of(entries), clock::get);
    }

    private ContextMenuEntry entry() {
        return new ContextMenuEntry(3060001, context -> true, callback);
    }

    @Test
    void invokesOfferedCallbackOnceWithResolvedItemContext() {
        var ui = ui(entry());
        ui.requestContextMenu(player, ITEM_SERIAL);

        ui.onContextMenuSelection(player, ITEM_SERIAL, 1);
        ui.onContextMenuSelection(player, ITEM_SERIAL, 1);

        verify(callback).accept(new ItemContextMenuContext(player, item));
    }

    @Test
    void ignoresUnsolicitedSelectionAndMissingPlayer() {
        var ui = ui(entry());

        ui.onContextMenuSelection(null, ITEM_SERIAL, 1);
        ui.onContextMenuSelection(player, ITEM_SERIAL, 1);

        verifyNoInteractions(callback);
    }

    @Test
    void refusesHiddenOrUnknownEntriesAndWrongTarget() {
        var hidden = new ContextMenuEntry(3060002, context -> false, callback);
        var ui = ui(entry(), hidden);
        ui.requestContextMenu(player, ITEM_SERIAL);
        ui.onContextMenuSelection(player, ITEM_SERIAL, 2);
        ui.requestContextMenu(player, ITEM_SERIAL);
        ui.onContextMenuSelection(player, ITEM_SERIAL, 65535);
        ui.requestContextMenu(player, ITEM_SERIAL);
        ui.onContextMenuSelection(player, ITEM_SERIAL + 1, 3);

        verifyNoInteractions(callback);
    }

    @Test
    void rechecksPredicateBeforeExecuting() {
        var ui = ui(new ContextMenuEntry(3060001,
                context -> context instanceof ItemContextMenuContext target && target.target().isInContainer(),
                callback));
        when(item.isInContainer()).thenReturn(true);
        ui.requestContextMenu(player, ITEM_SERIAL);
        when(item.isInContainer()).thenReturn(false);

        ui.onContextMenuSelection(player, ITEM_SERIAL, 1);
        when(item.isInContainer()).thenReturn(true);
        ui.onContextMenuSelection(player, ITEM_SERIAL, 1);

        verifyNoInteractions(callback);
    }

    @Test
    void rejectsDeletedOrReplacedTarget() {
        var ui = ui(entry());
        ui.requestContextMenu(player, ITEM_SERIAL);
        when(storage.items().get(ITEM_SERIAL)).thenReturn(Optional.empty());
        ui.onContextMenuSelection(player, ITEM_SERIAL, 1);
        when(storage.items().get(ITEM_SERIAL)).thenReturn(Optional.of(item));
        ui.requestContextMenu(player, ITEM_SERIAL);
        when(storage.items().get(ITEM_SERIAL)).thenReturn(Optional.of(mock(UOItem.class)));
        ui.onContextMenuSelection(player, ITEM_SERIAL, 2);

        verifyNoInteractions(callback);
    }

    @Test
    void expiresMenusAndReplacesThemOnNewRequests() {
        var ui = ui(entry());
        ui.requestContextMenu(player, ITEM_SERIAL);
        clock.set(Duration.ofSeconds(60).toNanos());
        ui.onContextMenuSelection(player, ITEM_SERIAL, 1);
        ui.requestContextMenu(player, ITEM_SERIAL);
        ui.requestContextMenu(player, 42);
        ui.onContextMenuSelection(player, ITEM_SERIAL, 1);

        verifyNoInteractions(callback);
    }

    @Test
    void selectsMobileEntryUsingCurrentMobileState() {
        var ui = ui(new ContextMenuEntry(3060001,
                context -> context instanceof MobileContextMenuContext target && target.target().isAlive(),
                callback));
        var mobile = mock(UOMobile.class);
        when(mobile.getSerialId()).thenReturn(42);
        when(mobile.isAlive()).thenReturn(true);
        when(storage.mobiles().get(42)).thenReturn(Optional.of(mobile));
        ui.requestContextMenu(player, 42);

        ui.onContextMenuSelection(player, 42, 1);

        verify(callback).accept(new MobileContextMenuContext(player, mobile));
    }
    @Test
    void independentEntriesWithSameClilocHaveDifferentSelectionIdentifiers() {
        Consumer<ContextMenuContext> otherCallback = mock();
        var ui = ui(entry(), new ContextMenuEntry(3060001, context -> true, otherCallback));
        ui.requestContextMenu(player, ITEM_SERIAL);
        var sent = ArgumentCaptor.forClass(ContextMenuRequested.class);
        verify(events).publish(sent.capture());
        var options = sent.getValue().entries();
        assertNotEquals(options.get(0).index(), options.get(1).index());

        ui.onContextMenuSelection(player, ITEM_SERIAL, options.get(1).index());

        verify(otherCallback).accept(new ItemContextMenuContext(player, item));
        verifyNoInteractions(callback);
    }

    @Test
    void rejectsPreviousMenuIdentifierForTheSameTarget() {
        var ui = ui(entry());
        ui.requestContextMenu(player, ITEM_SERIAL);
        ui.requestContextMenu(player, ITEM_SERIAL);
        var sent = ArgumentCaptor.forClass(ContextMenuRequested.class);
        verify(events, times(2)).publish(sent.capture());
        int oldIndex = sent.getAllValues().get(0).entries().getFirst().index();
        int newIndex = sent.getAllValues().get(1).entries().getFirst().index();
        assertNotEquals(oldIndex, newIndex);

        ui.onContextMenuSelection(player, ITEM_SERIAL, oldIndex);

        verifyNoInteractions(callback);
    }

    @Test
    void limitsVisibleEntriesInsteadOfTheGlobalRegistry() {
        var ui = ui();
        var definitions = new ArrayList<ContextMenuEntry>();
        for (int i = 0; i < 255; i++) {
            definitions.add(entry());
        }
        definitions.add(new ContextMenuEntry(3060002, context -> false));
        ui.registerContextMenuEntries(definitions);
        ui.requestContextMenu(player, ITEM_SERIAL);
        var sent = ArgumentCaptor.forClass(ContextMenuRequested.class);
        verify(events).publish(sent.capture());
        assertEquals(255, sent.getValue().entries().size());
        assertEquals(255, sent.getValue().entries().stream().map(ContextMenuRequested.Entry::index).distinct().count());

        definitions.add(entry());
        ui.registerContextMenuEntries(definitions);
        assertThrows(IllegalArgumentException.class, () -> ui.requestContextMenu(player, ITEM_SERIAL));
    }

}
