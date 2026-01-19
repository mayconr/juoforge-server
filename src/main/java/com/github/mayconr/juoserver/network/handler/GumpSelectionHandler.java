package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.gump.GumpSystemCallback;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.GumpSelection;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ChannelHandler.Sharable
public class GumpSelectionHandler extends PlayerSessionChannelInboundHandler<GumpSelection> {

    private final GumpSystemCallback gumpSystemCallback;

    @Override
    protected void channelRead0(
            PlayerSession session, ChannelHandlerContext ctx, GumpSelection msg) {
        gumpSystemCallback.onGumpSelection(ctx.channel(), msg);
    }
}
