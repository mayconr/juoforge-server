package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.MobileResurrectEvent;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.*;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResurrectionPacketsSessionTest {
    @Test
    void onlyResurrectedPlayersSessionReceivesResurrectionScreen() {
        var world = mock(WorldInternal.class, RETURNS_DEEP_STUBS);
        var settings = mock(GamePlaySettings.class, RETURNS_DEEP_STUBS);
        when(settings.world().visibility().range()).thenReturn(18);
        var targetData = new UOMobileData();
        targetData.setSerialId(1);
        targetData.setAlive(true);
        targetData.setStatus(CharacterStatus.NORMAL);
        var target = new UOPlayer(targetData);
        var observerData = new UOMobileData();
        observerData.setSerialId(2);
        observerData.setStatus(CharacterStatus.NORMAL);
        var observer = new UOPlayer(observerData);
        when(world.mobile().getEquippedItems(target)).thenReturn(Map.of());
        var targetChannel = new EmbeddedChannel();
        var observerChannel = new EmbeddedChannel();
        try {
            var ownSession = new NettyPlayerSession(targetChannel, null, settings, null, world);
            var otherSession = new NettyPlayerSession(observerChannel, null, settings, null, world);
            ReflectionTestUtils.setField(ownSession, "player", target);
            ReflectionTestUtils.setField(otherSession, "player", observer);
            var event = new MobileResurrectEvent(target);
            ownSession.onMobileResurrect(event);
            otherSession.onMobileResurrect(event);
            targetChannel.runPendingTasks();
            observerChannel.runPendingTasks();

            assertInstanceOf(StatusBarInfo.class, targetChannel.readOutbound());
            assertInstanceOf(DeathScreen.class, targetChannel.readOutbound());
            assertInstanceOf(DrawGamePlayer.class, targetChannel.readOutbound());
            assertInstanceOf(DrawMobile.class, targetChannel.readOutbound());
            assertNull(targetChannel.readOutbound());
            assertInstanceOf(DrawMobile.class, observerChannel.readOutbound());
            assertNull(observerChannel.readOutbound());
        } finally {
            targetChannel.finishAndReleaseAll();
            observerChannel.finishAndReleaseAll();
        }
    }
}
