package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.network.packet.DeleteCharacter;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class DeleteCharacterHandler extends PlayerSessionChannelInboundHandler<DeleteCharacter> {
    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, DeleteCharacter msg) {
        session.deleteCharacter(msg);
    }
}
