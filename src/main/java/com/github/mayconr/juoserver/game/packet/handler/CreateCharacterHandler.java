package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.core.model.PlayerDetails;
import com.github.mayconr.juoserver.game.packet.CreateCharacter;
import com.github.mayconr.juoserver.game.packet.LoginReject;
import com.github.mayconr.juoserver.game.storage.WorldService;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class CreateCharacterHandler extends SimpleChannelInboundHandler<CreateCharacter> {

    private final WorldService worldService;

    public CreateCharacterHandler(WorldService worldService) {
        this.worldService = worldService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CreateCharacter msg) throws Exception {
        final var account = ctx.channel().attr(AttributeKeys.ACCOUNT_LOGGED_IN).get();
        final var details = new PlayerDetails(account, "pass", msg.getCharacterName());
        worldService.createPlayer(
                details); // TODO initialize session if gameplay be started automatically
        ctx.writeAndFlush(new LoginReject(LoginReject.Reason.CHAR_ALREADY_EXIST));
    }
}
