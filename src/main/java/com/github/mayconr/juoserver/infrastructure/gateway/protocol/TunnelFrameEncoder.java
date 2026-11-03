package com.github.mayconr.juoserver.infrastructure.gateway.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public final class TunnelFrameEncoder extends MessageToByteEncoder<TunnelFrame> {
  private final int max;

  public TunnelFrameEncoder(int max) {
    this.max = max;
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, TunnelFrame frame, ByteBuf out) {
    byte[] data = frame.payload();
    if (data.length > max - 14) throw new IllegalArgumentException("Frame exceeds limit");
    out.writeInt(10 + data.length)
        .writeByte(TunnelFrame.VERSION)
        .writeByte(frame.type().ordinal())
        .writeLong(frame.sessionId())
        .writeBytes(data);
  }
}
