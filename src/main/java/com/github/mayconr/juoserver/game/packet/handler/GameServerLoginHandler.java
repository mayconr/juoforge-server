package com.github.mayconr.juoserver.game.packet.handler;

import java.util.HashMap;

import com.github.mayconr.juoserver.game.core.database.Database;
import com.github.mayconr.juoserver.game.core.model.CharacterListFlag;
import com.github.mayconr.juoserver.game.core.model.UOPlayer;
import com.github.mayconr.juoserver.game.packet.CharacterList;
import com.github.mayconr.juoserver.game.packet.GameServerLogin;
import com.github.mayconr.juoserver.game.packet.LoginReject;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class GameServerLoginHandler extends SimpleChannelInboundHandler<GameServerLogin> {

    private final Database database;

    public GameServerLoginHandler(Database database) {
        this.database = database;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GameServerLogin msg) throws Exception {
        database.getAccount(msg.getUsername(), msg.getPassword())
                .ifPresentOrElse(
                        account -> {
                            ctx.channel().attr(AttributeKeys.ACCOUNT_LOGGED_IN).set(account);
                            final var players = database.getPlayersByAccount(account);

                            // Prepare slots
                            final var slots = new HashMap<Integer, UOPlayer>();
                            int counter = 0;
                            for (UOPlayer character : players) {
                                slots.put(counter++, character);
                            }
                            ctx.channel().attr(AttributeKeys.CHARACTERS_SLOT).set(slots);

                            ctx.writeAndFlush(
                                    new CharacterList(
                                            players,
                                            database.getCities(),
                                            CharacterListFlag.ENABLE_AOS_COMMON,
                                            CharacterListFlag.SAMURAI_NINJA_CLASSES,
                                            CharacterListFlag.ENABLE_NPC_POPUP,
                                            CharacterListFlag.ELVEN_RACE));
                        },
                        () ->
                                ctx.writeAndFlush(
                                        new LoginReject(
                                                LoginReject.Reason.COULD_NOT_ATTACH_SERVER)));
    }
}
