package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.ActionSubCommand;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.player.SessionOutbound;
import com.github.mayconr.juoserver.game.player.PlayerSession;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.ActionRequest;
import com.github.mayconr.juoserver.network.packet.RequestHelp;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ChannelHandler.Sharable
public class RequestHelpHandler extends PlayerSessionChannelInboundHandler<RequestHelp> {

    private final WorldInternal  world;

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, RequestHelp msg) {
        world.handleAction((UOPlayer) session.getPlayer(), new ActionRequest(ActionSubCommand.HELP_BUTTON));
    }
}
