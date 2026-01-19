package com.github.mayconr.juoserver.game.session.npc;

import com.github.mayconr.juoserver.game.ai.NpcAI;
import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.session.game.GameSession;
import com.github.mayconr.juoserver.network.packet.DrawMobile;
import com.github.mayconr.juoserver.network.packet.SendSpeech;

import io.netty.channel.group.ChannelGroup;

public class DefaultNpcSession implements NpcSession {

    private final UONpc npc;
    private final ChannelGroup channelGroup;
    private final EventBus eventBus;
    private final NpcAI npcAI;
    private final MovementService movementService;

    public DefaultNpcSession(
            GameSession gameSession,
            UONpc npc,
            ChannelGroup channelGroup,
            EventBus eventBus,
            NpcAI npcAI,
            MovementService movementService) {
        this.npc = npc;
        this.channelGroup = channelGroup;
        this.eventBus = eventBus;
        this.npcAI = npcAI;
        this.movementService = movementService;
        this.npcAI.initialize(gameSession, this);
    }

    @Override
    public UONpc getNpc() {
        return npc;
    }

    @Override
    public void walk(Direction direction) {
        System.out.println("npc andando");
    }

    @Override
    public void speech(String message) {
        channelGroup.writeAndFlush(new SendSpeech(npc, message));
    }

    @Override
    public void move(Direction direction) {
        npc.move(direction);
        channelGroup.writeAndFlush(new DrawMobile(npc));
    }

    @Override
    public void move(Location location) {}
}
