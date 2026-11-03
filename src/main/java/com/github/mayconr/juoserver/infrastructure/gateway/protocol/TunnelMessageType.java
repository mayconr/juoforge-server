package com.github.mayconr.juoserver.infrastructure.gateway.protocol;

public enum TunnelMessageType {
  REGISTER,
  REGISTER_ACK,
  SESSION_OPEN,
  SESSION_ACCEPT,
  SESSION_REJECT,
  SESSION_CLOSE,
  DATA,
  HEARTBEAT,
  ERROR;

  public static TunnelMessageType from(int id) {
    if (id < 0 || id >= values().length) throw new IllegalArgumentException("Unknown message type");
    return values()[id];
  }
}
