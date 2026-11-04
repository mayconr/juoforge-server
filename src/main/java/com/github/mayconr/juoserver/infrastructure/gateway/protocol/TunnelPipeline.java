package com.github.mayconr.juoserver.infrastructure.gateway.protocol;

import io.netty.channel.*;
import io.netty.handler.timeout.*;
import java.util.concurrent.TimeUnit;

public final class TunnelPipeline {
  private TunnelPipeline() {}

  public static void install(Channel ch, TunnelSettings s, ChannelHandler handler) {
    ch.config()
        .setWriteBufferWaterMark(new WriteBufferWaterMark(s.bufferBytes() / 2, s.bufferBytes()));
    ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(s.maxFrameBytes() - 14));
    ch.pipeline()
        .addLast(
            new IdleStateHandler(
                s.idleTimeout().toMillis(), s.heartbeat().toMillis(), 0, TimeUnit.MILLISECONDS),
            new TunnelFrameDecoder(s.maxFrameBytes()),
            new TunnelFrameEncoder(s.maxFrameBytes()),
            new ChannelDuplexHandler() {
              @Override
              public void userEventTriggered(ChannelHandlerContext ctx, Object event)
                  throws Exception {
                if (event instanceof IdleStateEvent idle) {
                  if (idle.state() == IdleState.READER_IDLE) {
                    org.slf4j.LoggerFactory.getLogger(TunnelPipeline.class).warn(
                        "Gateway tunnel at {} closed: no incoming data or heartbeat for {} ms",
                        ctx.channel().remoteAddress(), s.idleTimeout().toMillis());
                    ctx.close();
                  }
                  else ctx.writeAndFlush(TunnelFrame.control(TunnelMessageType.HEARTBEAT, 0));
                } else super.userEventTriggered(ctx, event);
              }
            },
            handler);
  }
}
