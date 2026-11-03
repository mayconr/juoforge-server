package com.github.mayconr.juoserver.infrastructure.gateway.protocol;

import java.util.Objects;

/** Owns an immutable copy; callers never need to retain or release a frame. */
public record TunnelFrame(TunnelMessageType type, long sessionId, byte[] payload) {
  public static final int VERSION = 1, HEADER_SIZE = 10;

  public TunnelFrame {
    Objects.requireNonNull(type);
    payload = payload.clone();
  }

  @Override
  public byte[] payload() {
    return payload.clone();
  }

  public static TunnelFrame control(TunnelMessageType type, long id) {
    return new TunnelFrame(type, id, new byte[0]);
  }
}
