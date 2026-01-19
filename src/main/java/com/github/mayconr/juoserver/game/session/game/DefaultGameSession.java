package com.github.mayconr.juoserver.game.session.game;

import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.event.NpcSessionCreated;
import com.github.mayconr.juoserver.common.event.PlayerSessionClosed;
import com.github.mayconr.juoserver.common.event.PlayerSessionCreated;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.npc.NpcSession;
import com.github.mayconr.juoserver.game.session.npc.NpcSessionFactory;
import com.github.mayconr.juoserver.game.session.player.DefaultPlayerSession;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.session.player.PlayerSessionFactory;
import com.github.mayconr.juoserver.game.world.WorldService;
import com.github.mayconr.juoserver.network.packet.DrawMobile;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class DefaultGameSession implements GameSession {

    private final WorldService worldService;
    private final ChannelGroup channelGroup;
    private final SessionFanout fanout;
    private final EventBus eventBus;
    private final PlayerSessionFactory playerSessionFactory;
    private final NpcSessionFactory npcSessionFactory;
    private final Map<UONpc, NpcSession> npcNpcSessionMap = new HashMap<>();
    private final Map<UOPlayer, PlayerSession> playerSessionMap = new HashMap<>();

    // Services
    private final MessageService messageService;
    private final ItemService itemService;

    @Override
    public void initialize() {
        itemService.initialize();
    }

    @Override
    public void sendBroadcastMessage(String message) {
        messageService.handleSendBreadcastMessage(message);
    }

    @Override
    public PlayerSession getPlayerSession(UOMobile mobile) {
        if (mobile instanceof UOPlayer) {
            return playerSessionMap.get(mobile);
        }
        throw new IllegalArgumentException("Mobile is not a player");
    }

    @Override
    public CompletableFuture<NpcSession> createNpcSession(String name, Location location) {
        return worldService.createNpcAtLocation(name, location)
            .thenApply(npc -> {
                final var session = npcNpcSessionMap.putIfAbsent(npc, npcSessionFactory.create(this, npc));
                channelGroup.writeAndFlush(new DrawMobile(npc));
                eventBus.publish(new NpcSessionCreated(session));
                return session;
            })
            .whenComplete(((npcSession, throwable) -> {
                if (throwable != null) {
                    log.error("Unable to create session for [{}]",name, throwable);
                }
            }));
    }

    @Override
    public PlayerSession createPlayerSession(UOPlayer player, SessionOutbound outbound) {
        return playerSessionMap.computeIfAbsent(player, pl -> {
            final var session = (DefaultPlayerSession) playerSessionFactory.createPlayerSession(pl, outbound, fanout);

            outbound.onChannelClosed(()->{
                session.setActive(false);
                playerSessionMap.remove(pl);
                eventBus.publish(new PlayerSessionClosed(session));
                log.info(
                        "Session closed for mobile [{}-{}]",
                        pl.getSerialId(),
                        pl.getName());
            });
            session.setActive(true);
            eventBus.publish(new PlayerSessionCreated(session));

            return session;
        });
    }

    @Override
    public CompletableFuture<UOItem> createItemAtLocation(String name, Location location) {
        return itemService.handleCreateItemAtLocation(name, location);
    }

    @Override
    public void deleteItem(UOItem item) {
        itemService.handleDeleteItem(item);
    }

    @Override
    public void moveItem(UOItem item, Location location) {
        itemService.handleMoveItem(item, location);
    }
}
