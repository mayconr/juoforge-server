package com.github.mayconr.juoserver.game.core.session.game;

import java.util.HashMap;
import java.util.Map;

import com.github.mayconr.juoserver.game.core.database.Database;
import com.github.mayconr.juoserver.game.core.event.EventBus;
import com.github.mayconr.juoserver.game.core.event.NpcSessionCreated;
import com.github.mayconr.juoserver.game.core.event.PlayerSessionClosed;
import com.github.mayconr.juoserver.game.core.event.PlayerSessionCreated;
import com.github.mayconr.juoserver.game.core.model.*;
import com.github.mayconr.juoserver.game.core.session.npc.NpcSession;
import com.github.mayconr.juoserver.game.core.session.npc.NpcSessionFactory;
import com.github.mayconr.juoserver.game.core.session.player.DefaultPlayerSession;
import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.core.session.player.PlayerSessionFactory;
import com.github.mayconr.juoserver.game.packet.DrawMobile;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DefaultGameSession implements GameSession {

    private final Database database;
    private final ChannelGroup channelGroup;
    private final EventBus eventBus;
    private final PlayerSessionFactory playerSessionFactory;
    private final NpcSessionFactory npcSessionFactory;
    private final Map<UONpc, NpcSession> npcNpcSessionMap = new HashMap<>();
    private final Map<UOPlayer, PlayerSession> playerSessionMap = new HashMap<>();

    // Services
    private final MessageService messageService;
    private final ItemService itemService;

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
    public NpcSession createNpcSession(String name, Location location) {
        final var npc = database.createNpcAtLocation(name, location);
        try {
            final var session =
                    npcNpcSessionMap.putIfAbsent(npc, npcSessionFactory.create(this, npc));
            channelGroup.writeAndFlush(new DrawMobile(npc));
            eventBus.publish(new NpcSessionCreated(session));
            return session;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create AI for [" + npc.getAi() + "]", e);
        }
    }

    @Override
    public PlayerSession createPlayerSession(UOPlayer player, ChannelHandlerContext ctx) {
        return playerSessionMap.computeIfAbsent(
                player,
                pl -> {
                    final var session =
                            (DefaultPlayerSession)
                                    playerSessionFactory.createPlayerSession(pl, ctx);
                    ctx.channel()
                            .closeFuture()
                            .addListener(
                                    future -> {
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
    public UOItem createItemAtLocation(String name, Location location) {
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
