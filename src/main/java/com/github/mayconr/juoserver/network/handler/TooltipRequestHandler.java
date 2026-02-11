package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.player.SessionOutbound;
import com.github.mayconr.juoserver.game.player.PlayerSession;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.TooltipRequest;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class TooltipRequestHandler extends PlayerSessionChannelInboundHandler<TooltipRequest> {

    private final WorldInternal world;

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, TooltipRequest msg) {
        if (log.isDebugEnabled()) {
            log.debug("Tooltip requested for serials {}", msg.getSerialList());
        }
        world.tooltipRequest((UOPlayer) session.getPlayer(), msg.getSerialList());
    }
}
