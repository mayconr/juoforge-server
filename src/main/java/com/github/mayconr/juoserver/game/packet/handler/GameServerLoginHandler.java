package com.github.mayconr.juoserver.game.packet.handler;

import java.util.HashMap;
import java.util.List;

import com.github.mayconr.juoserver.game.core.model.AccountLoginMobile;
import com.github.mayconr.juoserver.game.core.model.CharacterListFlag;
import com.github.mayconr.juoserver.game.core.model.UOAccount;
import com.github.mayconr.juoserver.game.packet.CharacterList;
import com.github.mayconr.juoserver.game.packet.GameServerLogin;
import com.github.mayconr.juoserver.game.packet.LoginReject;
import com.github.mayconr.juoserver.game.storage.WorldService;
import com.github.mayconr.juoserver.game.storage.account.AccountStorage;
import com.github.mayconr.juoserver.game.storage.mobile.MobileStorage;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class GameServerLoginHandler extends SimpleChannelInboundHandler<GameServerLogin> {

    private final AccountStorage accountStorage;
    private final MobileStorage mobileStorage;
    private final WorldService worldService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GameServerLogin msg) throws Exception {
        accountStorage
                .findByUsername(msg.getUsername())
                .whenComplete(
                        (acct, ex) -> {
                            if (ex != null) {
                                log.error(
                                        "Error to recovery account for {}", msg.getUsername(), ex);
                                return;
                            }
                            if (acct.isEmpty()) {
                                log.warn("Account not found for {}", msg.getUsername());
                                ctx.writeAndFlush(
                                        new LoginReject(
                                                LoginReject.Reason.COULD_NOT_ATTACH_SERVER));
                                return;
                            }
                            handleAcct(ctx, acct.get());
                        });
    }

    private void handleAcct(ChannelHandlerContext ctx, UOAccount account) {
        log.info("Account [{}] logged in", account.getUsername());
        ctx.channel().attr(AttributeKeys.ACCOUNT_LOGGED_IN).set(account);

        mobileStorage
                .findPlayersByAccount(account)
                .handle(
                        (entries, ex) -> {
                            if (ex != null) {
                                log.error(
                                        "Error to recovery entries for account {}",
                                        account.getUsername(),
                                        ex);
                                return null;
                            }
                            return entries;
                        })
                .thenAccept(mobs -> handleMobiles(ctx, mobs));
    }

    private void handleMobiles(ChannelHandlerContext ctx, List<AccountLoginMobile> mobiles) {
        final var slots = new HashMap<Integer, AccountLoginMobile>();
        int counter = 0;
        for (AccountLoginMobile mobile : mobiles) {
            slots.put(counter++, mobile);
        }
        ctx.channel().attr(AttributeKeys.CHARACTERS_SLOT).set(slots);

        ctx.writeAndFlush(
                new CharacterList(
                        mobiles,
                        worldService.getCities(),
                        CharacterListFlag.ENABLE_AOS_COMMON,
                        CharacterListFlag.SAMURAI_NINJA_CLASSES,
                        CharacterListFlag.ENABLE_NPC_POPUP,
                        CharacterListFlag.ELVEN_RACE));
    }
}
