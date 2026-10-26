package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.*;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeadPlayerPacketsTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void movementRedrawAndLoginUseSameBodyAndSerial(boolean alive) {
        var data = new UOMobileData();
        data.setSerialId(42);
        data.setModelId(400);
        data.setGhostModelId(402);
        data.setHue(123);
        data.setDirection(Direction.NORTH);
        data.setStatus(CharacterStatus.WAR_MODE);
        data.setNotoriety(Notoriety.INNOCENT);
        data.setAlive(alive);
        var player = new UOPlayer(data);
        var packets = new Packet[]{new DrawGamePlayer(player), new UpdatePlayer(player),
                new DrawMobile(player, Map.of()), new LoginConfirm(player, 7168, 4096)};
        int[] serialOffsets = {1, 1, 3, 1};
        int[] modelOffsets = {5, 5, 7, 9};
        for (int i = 0; i < packets.length; i++) {
            var buf = Unpooled.buffer();
            try {
                packets[i].writesTo(buf);
                assertEquals(42, buf.getInt(serialOffsets[i]));
                assertEquals(alive ? 400 : 402, buf.getUnsignedShort(modelOffsets[i]));
                if (i < 3) {
                    int hueOffset = new int[]{8, 13, 15}[i];
                    assertEquals(alive ? 123 : 0, buf.getUnsignedShort(hueOffset));
                }
                if (i == 0) assertEquals(0x40, buf.getUnsignedByte(10));
            } finally {
                buf.release();
            }
        }
    }
}
