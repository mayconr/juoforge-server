package com.github.mayconr.juoserver.infrastructure.gateway.client;

import com.github.mayconr.juoserver.infrastructure.gateway.protocol.*;
import io.netty.channel.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public final class TunnelClientHandler extends SimpleChannelInboundHandler<TunnelFrame> {
  private final GatewayTunnelClient owner;
  private final String shard, token;
  private final TunnelSessionListener listener;
  private final TunnelSettings settings;
  private TunnelSessionManager sessions;
  private boolean registered;
  private ScheduledFuture<?> deadline;

  TunnelClientHandler(
      GatewayTunnelClient owner,
      String shard,
      String token,
      TunnelSessionListener listener,
      TunnelSettings settings) {
    this.owner = owner;
    this.shard = shard;
    this.token = token;
    this.listener = listener;
    this.settings = settings;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    sessions = new TunnelSessionManager(ctx.channel(), listener, settings);
    deadline =
        ctx.executor()
            .schedule(
                () -> ctx.close(), settings.handshakeTimeout().toMillis(), TimeUnit.MILLISECONDS);
    ctx.writeAndFlush(
        new TunnelFrame(
            TunnelMessageType.REGISTER,
            0,
            (shard + '\0' + token).getBytes(StandardCharsets.UTF_8)));
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, TunnelFrame frame) {
    if (!registered) {
      if (frame.type() != TunnelMessageType.REGISTER_ACK) {
        ctx.close();
        return;
      }
      registered = true;
      deadline.cancel(false);
      owner.registered();
      return;
    }
    switch (frame.type()) {
      case HEARTBEAT -> {}
      case SESSION_OPEN -> sessions.open(frame.sessionId());
      case DATA -> sessions.data(frame.sessionId(), frame.payload());
      case SESSION_CLOSE -> sessions.close(frame.sessionId(), false);
      default -> ctx.close();
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    if (deadline != null) deadline.cancel(false);
    if (sessions != null) sessions.closeAll();
    owner.disconnected();
  }

  @Override
  public void channelWritabilityChanged(ChannelHandlerContext ctx) {
    if (sessions != null) sessions.writable();
    ctx.fireChannelWritabilityChanged();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    ctx.close();
  }
}
