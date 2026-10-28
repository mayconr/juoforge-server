package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.network.session.handler.WorldViewHandler;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.MobileMoved;
import com.github.mayconr.juoserver.game.model.event.PlayerLoggedIn;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.*;
import com.github.mayconr.juoserver.network.session.i18n.ClientLocale;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WorldViewHandlerTest {
    @ParameterizedTest
    @CsvSource({"true,false", "false,false", "false,true"})
    void preservesNearbyVisibilityPacketOrderAndSingleFlush(boolean login, boolean teleport) {
        var world = mock(WorldInternal.class, RETURNS_DEEP_STUBS);
        var settings = mock(GamePlaySettings.class, RETURNS_DEEP_STUBS);
        when(settings.world().visibility().range()).thenReturn(18);
        var player = mock(UOPlayer.class, RETURNS_DEEP_STUBS);
        when(player.isAlive()).thenReturn(true);
        when(player.isConnected()).thenReturn(true);
        var online = mock(UOPlayer.class);
        when(online.isAlive()).thenReturn(true);
        when(online.isConnected()).thenReturn(true);
        var offline = mock(UOPlayer.class);
        when(offline.isAlive()).thenReturn(true);
        var npc = mock(UONpc.class);
        when(npc.isAlive()).thenReturn(true);
        var dead = mock(UONpc.class);
        var item = mock(UOItem.class);
        when(world.storage().getMobilesInRange(eq(player), eq(18), any()))
                .thenReturn(List.of(player, online, offline, npc, dead));
        when(world.item().getItemsInRange(player, 18)).thenReturn(List.of(item));
        when(world.mobile().getEquippedItems(any())).thenReturn(Map.of());
        var flushCounter = new FlushCounter();
        var channel = new EmbeddedChannel(flushCounter);
        try {
            var handler = new WorldViewHandler(
                    new SessionView(() -> player, () -> ClientLocale.PT_BR, settings),
                    new SessionPacketWriter(channel, null), world, settings);
            if (login) {
                handler.onPlayerLoggedIn(new PlayerLoggedIn(player));
                assertInstanceOf(LoginConfirm.class, channel.readOutbound());
                assertInstanceOf(SeasonalInformation.class, channel.readOutbound());
            } else {
                handler.onMobileMoved(new MobileMoved(player, null, player, player, false, 7, teleport));
                if (!teleport) {
                    assertInstanceOf(MoveResyncAck.class, channel.readOutbound());
                }
            }
            assertInstanceOf(DrawMobile.class, channel.readOutbound());
            assertInstanceOf(DrawMobile.class, channel.readOutbound());
            assertInstanceOf(ObjectInfo.class, channel.readOutbound());
            if (login) {
                assertInstanceOf(SendSkill.class, channel.readOutbound());
                assertInstanceOf(DrawGamePlayer.class, channel.readOutbound());
                assertInstanceOf(DrawMobile.class, channel.readOutbound());
                assertInstanceOf(StatusBarInfo.class, channel.readOutbound());
                assertInstanceOf(LoginComplete.class, channel.readOutbound());
            } else {
                if (teleport) {
                    assertInstanceOf(DrawGamePlayer.class, channel.readOutbound());
                } else {
                    assertInstanceOf(UpdatePlayer.class, channel.readOutbound());
                }
            }
            assertNull(channel.readOutbound());
            assertEquals(1, flushCounter.count);
            verify(world.mobile(), never()).getEquippedItems(offline);
            verify(world.mobile(), never()).getEquippedItems(dead);
            verify(world.storage()).getMobilesInRange(eq(player), eq(18), argThat(filter ->
                    filter.test(online) && filter.test(npc) && !filter.test(offline) && !filter.test(dead)));
            verify(world.item()).getItemsInRange(player, 18);
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    private static class FlushCounter extends ChannelOutboundHandlerAdapter {
        private int count;

        @Override
        public void flush(ChannelHandlerContext ctx) throws Exception {
            count++;
            super.flush(ctx);
        }
    }
}
