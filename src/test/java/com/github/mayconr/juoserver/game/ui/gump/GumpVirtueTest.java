package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.GumpSent;
import com.github.mayconr.juoserver.game.model.event.VirtueSelected;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.i18n.MessageLocalizer;
import com.github.mayconr.juoserver.network.packet.GumpSelection;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class GumpVirtueTest {
    private GumpSelection response(int gumpId, int serial, int button, boolean compact) {
        var buffer = Unpooled.buffer();
        try {
            buffer.writeByte(0xB1).writeShort(compact ? 15 : 23);
            buffer.writeInt(serial).writeInt(gumpId).writeInt(button);
            if (!compact) buffer.writeInt(0).writeInt(0);
            return new GumpSelection(buffer);
        } finally {
            buffer.release();
        }
    }

    @Test
    void emitsVirtueClassWithHueAndTracksOfferedGraphics() {
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(new Page(0).add(new VirtueImage(0x6F, 50, 50).hue(2405)))
                .render(builder);
        assertTrue(builder.buildLayout().contains("{gumppic 0 0 111 hue=2405 class=virtuegumpitem}"));
        assertEquals(Set.of(0x6F), builder.build().virtueGraphics);
        assertThrows(UnsupportedOperationException.class, () -> builder.build().virtueGraphics.add(0x69));
        assertThrows(IllegalArgumentException.class, () -> new VirtueImage(1));
        assertThrows(IllegalArgumentException.class, () -> new VirtueImage(0x6F, 0, 50));
        assertThrows(IllegalArgumentException.class, () -> new VirtueImage(0x6F).hue(-1));
        assertThrows(IllegalArgumentException.class, () -> new VirtueImage(0x6F).hue(65536));
        var centered = new GumpBuilder();
        new DeclarativeGumpUI(new Page(0).add(new Panel(3000, 200, 200,
                DeclarativeGumpUI.VirtueImage(0x6F)).center())).render(centered);
        assertTrue(centered.buildLayout().contains("{gumppic 75 75 111 hue=0 class=virtuegumpitem}"));

    }

    @Test
    void acceptsOnlySpecialCompactPacketAndPreservesStandardReplyFormat() {
        var selection = response(0x1CD, 7, 0x6F, true);
        assertTrue(selection.isVirtueSelection());
        assertEquals(7, selection.getSerialId());
        assertEquals(0x6F, selection.getButtonId());
        assertTrue(selection.getSwitches().isEmpty());
        assertTrue(selection.getTextEntries().isEmpty());
        assertFalse(response(123, 7, 1, false).isVirtueSelection());
        assertThrows(CorruptedFrameException.class, () -> response(123, 7, 1, true));
    }

    @Test
    void validatesOfferedVirtueSerialExpiryAndRateWithoutConsumingGump() {
        var data = new UOMobileData();
        data.setSerialId(7);
        var player = new UOPlayer(data);
        var clock = new AtomicLong(1000);
        var bus = mock(EventBus.class);
        var handler = mock(GumpHandler.class);
        var sent = new AtomicReference<GumpSent>();
        doAnswer(invocation -> { sent.set(invocation.getArgument(0)); return null; })
                .when(bus).publish(any(GumpSent.class));
        var system = new DefaultGumpSystem(bus, new GamePlaySettings.Ui(), mock(MessageLocalizer.class), clock::get);
        var click = response(0x1CD, 7, 0x6F, true);
        system.onGumpSelection(player, click);
        verify(bus, never()).publish(any(VirtueSelected.class));
        system.send(player, new DeclarativeGumpUI(new Page(0).add(new VirtueImage(0x6F, 50, 50))), handler, 1);
        system.onGumpSelection(player, response(0x1CD, 8, 0x6F, true));
        system.onGumpSelection(player, response(0x1CD, 7, 0x69, true));
        verify(bus, never()).publish(any(VirtueSelected.class));
        system.onGumpSelection(player, click);
        system.onGumpSelection(player, click);
        verify(bus, times(1)).publish(new VirtueSelected(player, 0x6F));
        verify(handler, never()).handle(any(), any());
        assertNotNull(player.runtimeAttributes().get(DefaultGumpSystem.PLAYER_GUMP_ID));
        clock.set(1100);
        system.onGumpSelection(player, click);
        verify(bus, times(2)).publish(new VirtueSelected(player, 0x6F));
        clock.set(2000);
        system.onGumpSelection(player, click);
        verify(bus, times(2)).publish(new VirtueSelected(player, 0x6F));
    }

    @Test
    void closingNormalGumpRemovesVirtueAuthorization() {
        var player = new UOPlayer(new UOMobileData());
        var clock = new AtomicLong(1000);
        var bus = mock(EventBus.class);
        var handler = mock(GumpHandler.class);
        var sent = new AtomicReference<GumpSent>();
        doAnswer(invocation -> { sent.set(invocation.getArgument(0)); return null; })
                .when(bus).publish(any(GumpSent.class));
        var system = new DefaultGumpSystem(bus, new GamePlaySettings.Ui(), mock(MessageLocalizer.class), clock::get);
        system.send(player, new DeclarativeGumpUI(new Page(0).add(new VirtueImage(0x6F, 50, 50))), handler);
        system.onGumpSelection(player, response(sent.get().gumpId(), player.getSerialId(), 0, false));
        clock.set(1200);
        system.onGumpSelection(player, response(0x1CD, player.getSerialId(), 0x6F, true));
        verify(handler).handle(any(), any());
        verify(bus, never()).publish(any(VirtueSelected.class));
    }
}
