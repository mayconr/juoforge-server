package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.network.packet.DeleteCharacter;
import com.github.mayconr.juoserver.network.packet.LoginReject;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class DeleteCharacterHandler extends SimpleChannelInboundHandler<DeleteCharacter> {

    private final RealmStorage realmStorage;

    public DeleteCharacterHandler(RealmStorage realmStorage) {
        this.realmStorage = realmStorage;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DeleteCharacter msg) throws Exception {
        final var slots = ctx.channel().attr(AttributeKeys.CHARACTERS_SLOT).getAndSet(null);
        final var character = slots.get(msg.getSelectedSlot());
        if (character == null) {
            ctx.writeAndFlush(new LoginReject(LoginReject.Reason.SYNCHRONIZATION_ERROR));
            return;
        }
        // database.deleteMobile(character);
        ctx.writeAndFlush(new LoginReject(LoginReject.Reason.CHAR_DOES_NOT_EXIST));
    }
}
