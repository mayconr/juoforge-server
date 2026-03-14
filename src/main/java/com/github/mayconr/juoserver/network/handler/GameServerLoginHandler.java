package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.infrastructure.storage.AccountStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import com.github.mayconr.juoserver.network.packet.GameServerLogin;
import com.github.mayconr.juoserver.network.packet.LoginReject;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class GameServerLoginHandler extends PlayerSessionChannelInboundHandler<GameServerLogin> {

    private final MobileStorage mobileStorage;
    private final AccountStorage accountStorage;

    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, GameServerLogin msg) {
        accountStorage.findByUsername(msg.getUsername())
                .thenAccept(session::authenticate)
                .whenComplete((u, throwable) -> {
                    if (throwable != null) {
                        log.error("Unable to authenticate session for {}", msg.getUsername(), throwable);
                        session.reject(LoginReject.Reason.COULD_NOT_ATTACH_SERVER);
                    }
                });
    }

}
