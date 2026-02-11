package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.ServerProperties;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerSessionFactory {

    private final ServerProperties properties;

    public PlayerSession createPlayerSession(UOPlayer player, SessionOutbound outbound, SessionFanout fanout) {
        return new DefaultPlayerSession(player, outbound, fanout, properties);
    }
}
