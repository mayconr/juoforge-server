package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.player.SessionOutbound;
import com.github.mayconr.juoserver.game.player.PlayerSession;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.UseRequest;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ChannelHandler.Sharable
public class UseRequestHandler extends PlayerSessionChannelInboundHandler<UseRequest> {

    private final WorldInternal  world;

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, UseRequest msg) {
        switch (msg.getType()) {
            case SKILL -> {
                String[] parts = msg.getValue().split(" ");
                int skillId = Integer.parseInt(parts[0]);
                //int targetMode = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                world.useSkill((UOPlayer) session.getPlayer(), skillId);
            }
            case MACRO_SPELL -> System.out.println("speel");
            case ACTION -> System.out.println("Action");
            case OPEN_DOOR -> System.out.println("open door");
        }

    }
}
