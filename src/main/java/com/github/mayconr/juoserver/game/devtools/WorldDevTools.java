package com.github.mayconr.juoserver.game.devtools;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.Packet;
import java.util.List;

/** Low-level development tools, including packet delivery through the player's normal network pipeline. */
public interface WorldDevTools {
    /** Packet instances must not be modified after this asynchronous call. */
    void sendRawPacket(UOPlayer player, Packet packet);

    /** Sends packets in list order with one flush; copies the list, but not packet instances. */
    void sendRawPackets(UOPlayer player, List<? extends Packet> packets);
}
