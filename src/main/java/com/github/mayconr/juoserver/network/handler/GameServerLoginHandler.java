package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.AccountMobile;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.infrastructure.storage.AccountStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import com.github.mayconr.juoserver.network.packet.GameServerLogin;
import com.github.mayconr.juoserver.network.packet.LoginReject;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class GameServerLoginHandler extends PlayerSessionChannelInboundHandler<GameServerLogin> {

    private final MobileStorage mobileStorage;
    private final AccountStorage accountStorage;

    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, GameServerLogin msg) {
        accountStorage.findByUsername(msg.getUsername())
                .thenCompose(account-> mobileStorage.findPlayersByAccount(account)
                        .thenApply(mobiles->new SessionInfo(account, mobiles)))
                .thenAccept(sessionInfo -> {
                    session.authenticate(sessionInfo.account(), sessionInfo.players);
                }).whenComplete((u, throwable) -> {
                    if (throwable != null) {
                        log.error("Unable to authenticate session for {}", msg.getUsername(), throwable);
                        session.reject(LoginReject.Reason.COULD_NOT_ATTACH_SERVER);
                    }
                });
    }

    record SessionInfo(UOAccount account, List<AccountMobile> players) {}

}
