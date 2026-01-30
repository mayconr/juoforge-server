package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.network.packet.StatusBarInfo;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatusService {

    private final UOPlayer player;
    private final SessionOutbound outbound;

    public void sendStatusGump(int serialId) {
        outbound.writeAndFlush(new StatusBarInfo(player));
    }

}
