package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.AccountLoginMobile;
import com.github.mayconr.juoserver.game.model.CharacterListFlag;
import com.github.mayconr.juoserver.game.model.SessionCreationContext;
import com.github.mayconr.juoserver.game.model.UOCity;
import com.github.mayconr.juoserver.game.player.SessionOutbound;
import com.github.mayconr.juoserver.network.packet.CharacterList;
import com.github.mayconr.juoserver.network.packet.GameServerLogin;
import com.github.mayconr.juoserver.network.packet.LoginReject;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.storage.AccountStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class GameServerLoginHandler extends SimpleChannelInboundHandler<GameServerLogin> {

    private final AccountStorage accountStorage;
    private final MobileStorage mobileStorage;
    private final RealmStorage storage;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GameServerLogin msg) throws Exception {
        final var outbound = new SessionOutbound(ctx, ctx.channel(), ctx.executor());

        accountStorage.findByUsername(msg.getUsername())
                .thenApply(account -> {
                    ctx.channel().attr(AttributeKeys.SESSION_OUTBOUND_KEY).set(outbound);
                    outbound.attr().set(AttributeKeys.ACCOUNT_KEY, account);
                    return account;
                })
                .thenCompose(mobileStorage::findPlayersByAccount)
                .thenAccept(mobiles->handleMobiles(outbound, mobiles))
                .exceptionally(throwable -> {
                    log.warn("Account not found for {}", msg.getUsername());
                    outbound.writeAndFlush(new LoginReject(LoginReject.Reason.COULD_NOT_ATTACH_SERVER));
                    return null;
                });
    }

    private void handleMobiles(SessionOutbound outbound, List<AccountLoginMobile> mobiles) {
        final var mobileSlots = new HashMap<Integer, AccountLoginMobile>();
        int mobileCounter = 0;
        for (AccountLoginMobile mobile : mobiles) {
            mobileSlots.put(mobileCounter++, mobile);
        }

        final var citySlots = new HashMap<Integer, UOCity>();
        int cityCounter = 0;
        for (UOCity city : storage.getCities()) {
            citySlots.put(cityCounter++, city);
        }

        outbound.attr().set(AttributeKeys.SESSION_CREATION_CONTEXT, new SessionCreationContext(mobileSlots, citySlots));

        outbound.writeAndFlush(new CharacterList(
                mobiles,
                citySlots,
                CharacterListFlag.ENABLE_AOS_COMMON,
                CharacterListFlag.SAMURAI_NINJA_CLASSES,
                CharacterListFlag.ENABLE_NPC_POPUP,
                CharacterListFlag.ELVEN_RACE));
    }
}
