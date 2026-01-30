package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.ActionSubCommand;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.ActionRequest;
import com.github.mayconr.juoserver.network.packet.RequestHelp;
import io.netty.channel.ChannelHandler;

@ChannelHandler.Sharable
public class RequestHelpHandler extends PlayerSessionChannelInboundHandler<RequestHelp> {

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, RequestHelp msg) {
        session.handleAction(new ActionRequest(ActionSubCommand.HELP_BUTTON));
    }
}
