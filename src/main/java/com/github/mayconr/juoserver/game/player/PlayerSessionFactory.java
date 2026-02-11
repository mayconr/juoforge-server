package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.ServerProperties;
import com.github.mayconr.juoserver.game.gameloop.GameLoop;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.player.vitals.PlayerVitalsTask;
import com.github.mayconr.juoserver.game.player.vitals.VitalsService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerSessionFactory {

    private final GameLoop gameLoop;
    private final ServerProperties properties;

    public PlayerSession createPlayerSession(UOPlayer player, SessionOutbound outbound, SessionFanout fanout) {
        final var vitalsService = new VitalsService(player, outbound, properties);

        final var session =
                new DefaultPlayerSession(
                        player,
                        outbound,
                        fanout,
                        properties);
        // TODO remove all registered events
        gameLoop.addTask(new PlayerVitalsTask(session, vitalsService, properties));
        return session;
    }
}
