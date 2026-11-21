package com.github.mayconr.juoserver.network.session.handler;

import com.github.mayconr.juoserver.game.model.event.MobileResistancesChanged;
import com.github.mayconr.juoserver.network.packet.StatusBarInfo;
import com.github.mayconr.juoserver.network.session.SessionPacketWriter;
import com.github.mayconr.juoserver.network.session.SessionView;

public final class ResistanceEventHandler {
    private final SessionView session;
    private final SessionPacketWriter packetWriter;

    public ResistanceEventHandler(SessionView session, SessionPacketWriter packetWriter) {
        this.session = session;
        this.packetWriter = packetWriter;
    }

    public void onResistancesChanged(MobileResistancesChanged event) {
        if (session.isPlayer(event.mobile())) {
            packetWriter.enqueue(packets ->
                    packets.writeAndFlush(new StatusBarInfo(event.mobile(), session.settings().mobile())));
        }
    }

}
