package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.GumpExpired;
import com.github.mayconr.juoserver.game.model.event.GumpSent;
import com.github.mayconr.juoserver.game.ui.UIModuleImpl;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.i18n.MessageLocalizer;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.network.packet.GumpSelection;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GumpSystemTest {
    private final UOPlayer player = new UOPlayer(new UOMobileData());
    private final EventBus bus = mock(EventBus.class);
    private final DefaultGumpSystem system = new DefaultGumpSystem(bus, new GamePlaySettings.Ui(), mock(MessageLocalizer.class));
    private final GumpHandler handler = mock(GumpHandler.class);

    private int open() { return open(system); }

    private int open(DefaultGumpSystem gumps) { return open(gumps, null); }

    private int open(DefaultGumpSystem gumps, Double expirationSeconds) {
        var sent = new AtomicReference<GumpSent>();
        doAnswer(invocation -> { sent.set(invocation.getArgument(0)); return null; })
                .when(bus).publish(any(GumpSent.class));
        var ui = new DeclarativeGumpUI(new Page(0));
        if (expirationSeconds == null) {
            gumps.send(player, ui, handler);
        } else {
            new UIModuleImpl(gumps, mock(WorldStorage.class, RETURNS_DEEP_STUBS), mock(EventBus.class), List.of()).sendGump(player, ui, handler, expirationSeconds);
        }
        return sent.get().gumpId();
    }

    private GumpSelection response(int id, int serial, String text) {
        return response(id, serial, text, 1);
    }

    private GumpSelection response(int id, int serial, String text, int button) {
        var buf = Unpooled.buffer();
        try {
            buf.writeByte(0xB1).writeShort(27 + text.length() * 2);
            buf.writeInt(serial).writeInt(id).writeInt(button).writeInt(0).writeInt(1);
            buf.writeShort(42).writeShort(text.length());
            buf.writeCharSequence(text, StandardCharsets.UTF_16BE);
            return new GumpSelection(buf);
        } finally { buf.release(); }
    }

    private void assertReleased(DefaultGumpSystem gumps) throws Exception {
        assertNull(player.runtimeAttributes().get(DefaultGumpSystem.PLAYER_GUMP_ID));
        var pending = DefaultGumpSystem.class.getDeclaredField("pendingPlayers");
        pending.setAccessible(true);
        assertEquals(Set.of(), pending.get(gumps));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 2001, 2002})
    void closingResponsesReleaseTheCallbackAndPlayerBeforeInvocation(int button) throws Exception {
        int id = open();
        doAnswer(invocation -> { assertReleased(system); return null; })
                .when(handler).handle(any(), any());
        system.onGumpSelection(player, response(id, player.getSerialId(), "1", button));
        assertReleased(system);
        verify(handler).handle(any(), any());
    }

    @Test
    void callbackFailuresDoNotKeepTheGumpPending() throws Exception {
        int id = open();
        doThrow(new IllegalStateException("Listener failed")).when(handler).handle(any(), any());
        assertThrows(IllegalStateException.class,
                () -> system.onGumpSelection(player, response(id, player.getSerialId(), "")));
        assertReleased(system);
    }

    @Test
    void individualTimeoutsOverrideTheDefaultWithoutAffectingOtherGumps() {
        var clock = new AtomicLong(1000);
        var gumps = new DefaultGumpSystem(bus, new GamePlaySettings.Ui(), mock(MessageLocalizer.class), clock::get);
        int defaultId = open(gumps);
        int shortId = open(gumps, 1.5);
        int longId = open(gumps, 60.0);
        clock.set(2499);
        gumps.update(0.05);
        verify(bus, never()).publish(any(GumpExpired.class));
        clock.set(2500);
        gumps.update(0.05);
        verify(bus).publish(new GumpExpired(player, shortId));
        verify(bus, never()).publish(new GumpExpired(player, defaultId));
        clock.set(31000);
        gumps.update(0.05);
        verify(bus).publish(new GumpExpired(player, defaultId));
        verify(bus, never()).publish(new GumpExpired(player, longId));
        clock.set(61000);
        gumps.update(0.05);
        verify(bus).publish(new GumpExpired(player, longId));
    }

    @Test
    void responseValidationUsesTheIndividualTimeout() {
        var clock = new AtomicLong(1000);
        var gumps = new DefaultGumpSystem(bus, new GamePlaySettings.Ui(), mock(MessageLocalizer.class), clock::get);
        int shortId = open(gumps, 1.0);
        int longId = open(gumps, 60.0);
        clock.set(31000);
        gumps.onGumpSelection(player, response(shortId, player.getSerialId(), ""));
        verifyNoInteractions(handler);
        var valid = response(longId, player.getSerialId(), "");
        gumps.onGumpSelection(player, valid);
        verify(handler).handle(any(), same(valid));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.MAX_VALUE})
    void invalidIndividualTimeoutsDoNotRegisterOrSendAGump(double seconds) {
        assertThrows(IllegalArgumentException.class,
                () -> system.send(player, new DeclarativeGumpUI(new Page(0)), handler, seconds));
        verifyNoInteractions(bus);
        assertNull(player.runtimeAttributes().get(DefaultGumpSystem.PLAYER_GUMP_ID));
    }

    @Test
    void uiUpdateRemovesGumpsAfterThirtySecondsAndNotifiesOnce() {
        var clock = new AtomicLong(1000);
        var gumps = new DefaultGumpSystem(bus, new GamePlaySettings.Ui(), mock(MessageLocalizer.class), clock::get);
        var ui = new UIModuleImpl(gumps, mock(WorldStorage.class, RETURNS_DEEP_STUBS), mock(EventBus.class), List.of());
        int id = open(gumps);
        doAnswer(invocation -> {
            assertNull(player.runtimeAttributes().get(DefaultGumpSystem.PLAYER_GUMP_ID));
            return null;
        }).when(bus).publish(any(GumpExpired.class));
        clock.set(30999);
        ui.update(0.05);
        verify(bus, never()).publish(any(GumpExpired.class));
        clock.set(31000);
        ui.update(0.05);
        ui.update(0.05);
        verify(bus).publish(new GumpExpired(player, id));
        gumps.onGumpSelection(player, response(id, player.getSerialId(), ""));
        verifyNoInteractions(handler);
    }

    @Test
    void customTimeoutIsAppliedAndEachGumpKeepsItsOwnDeadline() {
        var clock = new AtomicLong(1000);
        var gumps = new DefaultGumpSystem(bus, new GamePlaySettings.Ui(5), mock(MessageLocalizer.class), clock::get);
        int first = open(gumps);
        clock.set(4000);
        int second = open(gumps);
        clock.set(6000);
        gumps.update(0.05);
        verify(bus).publish(new GumpExpired(player, first));
        verify(bus, never()).publish(new GumpExpired(player, second));
        assertNotNull(player.runtimeAttributes().get(DefaultGumpSystem.PLAYER_GUMP_ID));
        clock.set(9000);
        gumps.update(0.05);
        verify(bus).publish(new GumpExpired(player, second));
        assertNull(player.runtimeAttributes().get(DefaultGumpSystem.PLAYER_GUMP_ID));
    }

    @Test
    void answeredGumpsDoNotPublishExpirationEvents() {
        var clock = new AtomicLong(1000);
        var gumps = new DefaultGumpSystem(bus, new GamePlaySettings.Ui(), mock(MessageLocalizer.class), clock::get);
        int id = open(gumps);
        gumps.onGumpSelection(player, response(id, player.getSerialId(), ""));
        clock.set(40000);
        gumps.update(0.05);
        verify(bus, never()).publish(any(GumpExpired.class));
        assertNull(player.runtimeAttributes().get(DefaultGumpSystem.PLAYER_GUMP_ID));
        verify(handler).handle(any(), any());
    }

    @Test
    void expiredResponsesWaitForGameLoopCleanupAndNotification() {
        var clock = new AtomicLong(1000);
        var gumps = new DefaultGumpSystem(bus, new GamePlaySettings.Ui(), mock(MessageLocalizer.class), clock::get);
        int id = open(gumps);
        clock.set(31000);
        gumps.onGumpSelection(player, response(id, player.getSerialId(), ""));
        verifyNoInteractions(handler);
        verify(bus, never()).publish(any(GumpExpired.class));
        gumps.update(0.05);
        verify(bus).publish(new GumpExpired(player, id));
    }

    @Test
    void unknownAndRepeatedResponsesDoNotRunTheHandler() {
        int id = open();
        system.onGumpSelection(player, response(id + 1, player.getSerialId(), ""));
        var valid = response(id, player.getSerialId(), "");
        system.onGumpSelection(player, valid);
        system.onGumpSelection(player, valid);
        verify(handler).handle(any(), same(valid));
        verifyNoMoreInteractions(handler);
    }

    @Test
    void rejectedSerialAndTextDoNotConsumeThePendingGump() {
        int id = open();
        system.onGumpSelection(player, response(id, player.getSerialId() + 1, ""));
        system.onGumpSelection(player, response(id, player.getSerialId(), "bad\0text"));
        verifyNoInteractions(handler);
        var valid = response(id, player.getSerialId(), "x".repeat(239));
        system.onGumpSelection(player, valid);
        verify(handler).handle(any(), same(valid));
    }

    @Test
    void rateLimitedResponseCanBeRetried() {
        int id = open();
        player.runtimeAttributes().set(DefaultGumpSystem.PLAYER_LAST_GUMP_RESPONSE, Long.MAX_VALUE);
        var valid = response(id, player.getSerialId(), "");
        system.onGumpSelection(player, valid);
        verifyNoInteractions(handler);
        player.runtimeAttributes().remove(DefaultGumpSystem.PLAYER_LAST_GUMP_RESPONSE);
        system.onGumpSelection(player, valid);
        verify(handler).handle(any(), same(valid));
    }

    @Test
    void concurrentResponsesConsumeTheContextOnlyOnce() throws Exception {
        int id = open();
        var valid = response(id, player.getSerialId(), "");
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> { ready.countDown(); start.await(); system.onGumpSelection(player, valid); return null; });
            var second = executor.submit(() -> { ready.countDown(); start.await(); system.onGumpSelection(player, valid); return null; });
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            first.get(5, TimeUnit.SECONDS);
            second.get(5, TimeUnit.SECONDS);
        }
        verify(handler).handle(any(), same(valid));
        verifyNoMoreInteractions(handler);
    }

    @Test
    void builtTextListsAreSnapshots() {
        var builder = new GumpBuilder().page(0).text(0, 0, 0, "first");
        var built = builder.build();
        builder.text(0, 20, 0, "second");
        assertEquals(List.of("first"), built.texts);
        assertThrows(UnsupportedOperationException.class, () -> built.texts.add("third"));
    }

    @Test
    void rowsLayOutHorizontallyAndNestedColumnsRespectComponentHeight() {
        var row = new Row(5, List.of(new Label("aa"), new Label("bb")));
        row.layout(new LayoutContext(10, 20, 200, 100));
        var builder = new GumpBuilder().page(0);
        row.render(builder);
        assertTrue(builder.buildLayout().contains("{text 10 20 1152 0}"));
        assertTrue(builder.buildLayout().contains("{text 29 20 1152 1}"));
        assertEquals(33, row.getWidth());

        var column = new Column(5);
        column.add(new Panel(100, 50, new Label("panel")));
        column.add(FormField.stacked(new Label("label"), new TextField(42, 80)));
        column.add(new Label("last"));
        column.layout(new LayoutContext(10, 20, 200, 200));
        builder = new GumpBuilder().page(0);
        column.render(builder);
        assertTrue(builder.buildLayout().contains("{text 10 75 1152 1}"));
        assertTrue(builder.buildLayout().contains("{text 10 122 1152"));
        assertEquals(116, column.getHeight());
    }
}
