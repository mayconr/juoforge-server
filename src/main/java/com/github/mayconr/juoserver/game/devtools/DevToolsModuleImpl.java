package com.github.mayconr.juoserver.game.devtools;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.RawPacketSent;
import com.github.mayconr.juoserver.game.model.event.RawPacketsSent;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.network.packet.Packet;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
public class DevToolsModuleImpl implements DevToolsModule {
    private final EventBus eventBus;

    @Override
    public void sendRawPacket(UOPlayer player, Packet packet) {
        eventBus.publish(new RawPacketSent(player, packet));
    }

    @Override
    public void sendRawPackets(UOPlayer player, List<? extends Packet> packets) {
        eventBus.publish(new RawPacketsSent(player, List.copyOf(packets)));
    }
}
