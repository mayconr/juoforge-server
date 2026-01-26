package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.AccountLoginMobile;
import com.github.mayconr.juoserver.game.model.CharacterListFlag;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.network.packet.CharacterList;
import com.github.mayconr.juoserver.network.packet.GameServerLogin;
import com.github.mayconr.juoserver.network.packet.LoginReject;
import com.github.mayconr.juoserver.infrastructure.server.Future;
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
    private final RealmStorage realmStorage;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GameServerLogin msg) throws Exception {
        final var outbound = new SessionOutbound(ctx, ctx.channel(), ctx.executor());
        ctx.channel().attr(AttributeKeys.SESSION_OUTBOUND_KEY).set(outbound);

        Future.fire(accountStorage.findByUsername(msg.getUsername())
            .thenAccept(opt->{
                if (opt.isEmpty()) {
                    log.warn("Account not found for {}", msg.getUsername());
                    outbound.writeAndFlush(new LoginReject(LoginReject.Reason.COULD_NOT_ATTACH_SERVER));
                    return;
                }

                handleAcct(outbound, opt.get());
            })
        );
    }

    private void handleAcct(SessionOutbound outbound, UOAccount account) {
        log.info("Account [{}] logged in", account.getUsername());
        outbound.attr().set(AttributeKeys.ACCOUNT_KEY, account);

        Future.fire(mobileStorage.findPlayersByAccount(account)
            .thenAccept(players-> handleMobiles(outbound, players))
        );
    }

    private void handleMobiles(SessionOutbound outbound, List<AccountLoginMobile> mobiles) {
        final var slots = new HashMap<Integer, AccountLoginMobile>();
        int counter = 0;
        for (AccountLoginMobile mobile : mobiles) {
            slots.put(counter++, mobile);
        }
        outbound.attr().set(AttributeKeys.CHARACTERS_SLOT_KEY, slots);
        outbound.writeAndFlush(new CharacterList(
                mobiles,
                realmStorage.getCities(),
                CharacterListFlag.ENABLE_AOS_COMMON,
                CharacterListFlag.SAMURAI_NINJA_CLASSES,
                CharacterListFlag.ENABLE_NPC_POPUP,
                CharacterListFlag.ELVEN_RACE));
    }
}
