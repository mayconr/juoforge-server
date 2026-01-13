package com.github.mayconr.juoserver.game.packet;

import static com.github.mayconr.juoserver.game.PacketExpect.fromBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.mayconr.juoserver.game.FakeUOClient;
import com.github.mayconr.juoserver.game.server.UOChannelInitializer;

class PingPongTest {

    @Autowired private UOChannelInitializer channelInitializer;
    private FakeUOClient client;

    @BeforeEach
    public void setUp() {
        client = new FakeUOClient(channelInitializer);
    }

    // @Test
    public void shouldReceiveSameSequence() {
        client.send(new PingPong(1));
        final var buf = client.receiveRaw();
        fromBuf(buf)
                .packet(
                        0x73,
                        ping -> {
                            assertEquals(1, buf.readUnsignedByte());
                        })
                .end();
    }
}
