package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.ActionSubCommand;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.ActionRequest;
import com.github.mayconr.juoserver.network.packet.RequestHelp;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ChannelHandler.Sharable
public class RequestHelpHandler extends PlayerSessionChannelInboundHandler<RequestHelp> {

    private final WorldInternal  world;

    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, RequestHelp msg) {
        world.handleAction(session.getPlayer(), new ActionRequest(ActionSubCommand.HELP_BUTTON));
    }
}
