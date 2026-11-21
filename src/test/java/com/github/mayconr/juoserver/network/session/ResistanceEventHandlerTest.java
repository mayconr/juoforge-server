package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.model.ResistanceType;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.mobile.MobileModuleImpl;
import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.model.Race;
import com.github.mayconr.juoserver.game.model.Gender;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.MobileResistancesChanged;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import com.github.mayconr.juoserver.network.packet.StatusBarInfo;
import com.github.mayconr.juoserver.network.session.handler.ResistanceEventHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResistanceEventHandlerTest {
    @Test
    void resistanceEventSendsAllFiveUpdatedValuesOnlyToOwnersSession() {
        var data = new UOMobileData();
        data.setSerialId(1);
        data.setRace(Race.HUMAN);
        data.setGender(Gender.MALE);
        data.setDisplayName("Player");
        var player = new UOPlayer(data);
        var otherData = new UOMobileData();
        otherData.setSerialId(2);
        var other = new UOPlayer(otherData);
        var channel = new EmbeddedChannel();
        var observerChannel = new EmbeddedChannel();
        try {
            var settings = new GamePlaySettings().withMobile(new GamePlaySettings.Mobile(
                    null, null, null, null, 0, 70));
            var events = new DefaultEventBus();
            var handler = new ResistanceEventHandler(new SessionView(() -> player, () -> null, settings),
                    new SessionPacketWriter(channel, null));
            var observer = new ResistanceEventHandler(new SessionView(() -> other, () -> null, settings),
                    new SessionPacketWriter(observerChannel, null));
            events.register(MobileResistancesChanged.class, handler::onResistancesChanged);
            events.register(MobileResistancesChanged.class, observer::onResistancesChanged);
            WorldMobile mobiles = new MobileModuleImpl(null, null, events, null, settings);
            mobiles.setResistance(player, ResistanceType.PHYSICAL, 5);
            mobiles.setResistance(player, ResistanceType.FIRE, 4);
            mobiles.setResistance(player, ResistanceType.COLD, 3);
            mobiles.setResistance(player, ResistanceType.POISON, 2);
            mobiles.setResistance(player, ResistanceType.ENERGY, 1);
            channel.runPendingTasks();
            observerChannel.runPendingTasks();
            var packet = assertInstanceOf(StatusBarInfo.class, channel.readOutbound());
            for (int index = 1; index < ResistanceType.values().length; index++) {
                assertInstanceOf(StatusBarInfo.class, channel.readOutbound());
            }
            var buffer = Unpooled.buffer();
            try {
                packet.writesTo(buffer);
                assertEquals(5, buffer.getUnsignedShort(62));
                assertEquals(4, buffer.getUnsignedShort(73));
                assertEquals(3, buffer.getUnsignedShort(75));
                assertEquals(2, buffer.getUnsignedShort(77));
                assertEquals(1, buffer.getUnsignedShort(79));
                for (int offset = 91; offset < 101; offset += Short.BYTES) {
                    assertEquals(70, buffer.getShort(offset));
                }
            } finally {
                buffer.release();
            }
            assertNull(channel.readOutbound());
            assertNull(observerChannel.readOutbound());
        } finally {
            channel.finishAndReleaseAll();
            observerChannel.finishAndReleaseAll();
        }
    }
}
