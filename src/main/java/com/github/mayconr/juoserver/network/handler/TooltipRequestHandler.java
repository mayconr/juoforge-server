package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.TooltipRequest;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class TooltipRequestHandler extends PlayerSessionChannelInboundHandler<TooltipRequest> {

    private final WorldInternal world;

    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, TooltipRequest msg) {
        if (log.isDebugEnabled()) {
            log.debug("Tooltip requested for serials {}", msg.getSerialList());
        }
        world.tooltipRequest(session.getPlayer(), msg.getSerialList());
    }
}
