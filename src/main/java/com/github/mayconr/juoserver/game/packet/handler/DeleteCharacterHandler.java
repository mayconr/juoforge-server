package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.core.database.Database;
import com.github.mayconr.juoserver.game.packet.DeleteCharacter;
import com.github.mayconr.juoserver.game.packet.LoginReject;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class DeleteCharacterHandler extends SimpleChannelInboundHandler<DeleteCharacter> {

    private final Database database;

    public DeleteCharacterHandler(Database database) {
        this.database = database;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DeleteCharacter msg) throws Exception {
        final var slots = ctx.channel().attr(AttributeKeys.CHARACTERS_SLOT).getAndSet(null);
        final var character = slots.get(msg.getSelectedSlot());
        if (character == null) {
            ctx.writeAndFlush(new LoginReject(LoginReject.Reason.SYNCHRONIZATION_ERROR));
            return;
        }
        database.deleteMobile(character);
        ctx.writeAndFlush(new LoginReject(LoginReject.Reason.CHAR_DOES_NOT_EXIST));
    }
}
