package com.github.mayconr.juoserver.infrastructure.gateway.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public final class TunnelFrameDecoder extends LengthFieldBasedFrameDecoder {
  public TunnelFrameDecoder(int max) {
    super(max, 0, 4, 0, 4);
  }

  @Override
  protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
    ByteBuf frame = (ByteBuf) super.decode(ctx, in);
    if (frame == null) return null;
    try {
      if (frame.readableBytes() < 10 || frame.readUnsignedByte() != TunnelFrame.VERSION)
        throw new CorruptedFrameException("Invalid tunnel header");
      TunnelMessageType type;
      try {
        type = TunnelMessageType.from(frame.readUnsignedByte());
      } catch (IllegalArgumentException e) {
        throw new CorruptedFrameException("Unknown message type");
      }
      long id = frame.readLong();
      if (id < 0) throw new CorruptedFrameException("Invalid session id");
      boolean session =
          switch (type) {
            case SESSION_OPEN, SESSION_ACCEPT, SESSION_REJECT, SESSION_CLOSE, DATA -> true;
            default -> false;
          };
      if (session != (id != 0)) throw new CorruptedFrameException("Invalid message scope");
      byte[] data = new byte[frame.readableBytes()];
      frame.readBytes(data);
      if (type != TunnelMessageType.DATA && type != TunnelMessageType.REGISTER && data.length != 0)
        throw new CorruptedFrameException("Unexpected control payload");
      return new TunnelFrame(type, id, data);
    } finally {
      frame.release();
    }
  }
}
