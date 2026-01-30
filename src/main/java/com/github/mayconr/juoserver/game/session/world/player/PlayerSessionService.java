package com.github.mayconr.juoserver.game.session.world.player;

import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.event.PlayerSessionClosed;
import com.github.mayconr.juoserver.common.event.PlayerSessionCreated;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.player.DefaultPlayerSession;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.session.player.PlayerSessionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class PlayerSessionService {

    private final Map<Integer, PlayerSession> playerSessionMap = new ConcurrentHashMap<>();
    private final PlayerSessionFactory playerSessionFactory;
    private final EventBus eventBus;
    private final SessionFanout fanout;

    public CompletableFuture<PlayerSession> create(UOPlayer player, SessionOutbound outbound) {
        return CompletableFuture.completedFuture(playerSessionMap.computeIfAbsent(player.getSerialId(), serial -> {
            final var session = (DefaultPlayerSession) playerSessionFactory.createPlayerSession(player, outbound, fanout);

            outbound.onChannelClosed(()->{
                session.setActive(false);
                playerSessionMap.remove(serial);
                eventBus.publish(new PlayerSessionClosed(session));
                log.info("Session closed for player [{}-{}]", player.getSerialId(), player.getName());
            });
            session.setActive(true);
            eventBus.publish(new PlayerSessionCreated(session));

            return session;
        }));
    }

    public PlayerSession getSession(UOPlayer player) {
        return playerSessionMap.get(player.getSerialId());
    }
}
