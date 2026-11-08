package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.player.PlayerModule;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.i18n.ClientLocale;
import com.github.mayconr.juoserver.support.TestFields;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionLocalePropagationTest {
    @Test
    void transfersTheSessionLanguageOnAttachAndOnLaterChanges() {
        var channel = new EmbeddedChannel();
        try {
            var world = mock(WorldInternal.class);
            var players = mock(PlayerModule.class);
            when(world.player()).thenReturn(players);
            var player = new UOPlayer(new UOMobileData());
            when(players.loadMobile(1)).thenReturn(CompletableFuture.completedFuture(player));
            var session = new NettyPlayerSession(channel, null, null, mock(EventBus.class), world);
            session.setLocale(ClientLocale.PT_BR);
            TestFields.setField(session, "state", SessionState.CHARACTER_SELECTED);
            TestFields.setField(session, "mobileSerialId", 1);
            assertSame(player, session.enteringWorld().join());
            assertEquals(ClientLocale.PT_BR, player.getLocale());
            session.setLocale(ClientLocale.ES_ES);
            assertEquals(ClientLocale.ES_ES, player.getLocale());
        } finally {
            channel.finishAndReleaseAll();
        }
    }
}
