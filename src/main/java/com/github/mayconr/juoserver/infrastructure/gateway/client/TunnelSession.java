package com.github.mayconr.juoserver.infrastructure.gateway.client;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

public final class TunnelSession {
  private final long id;
  private final TunnelSessionManager manager;
  final AtomicInteger pending = new AtomicInteger();

  TunnelSession(long id, TunnelSessionManager manager) {
    this.id = id;
    this.manager = manager;
  }

  public long id() {
    return id;
  }

  /** Completes after all bytes have been written; excess pending data closes this session. */
  public CompletionStage<Void> send(byte[] data) {
    return manager.send(this, data);
  }

  public void close() {
    manager.requestClose(this);
  }

  public boolean isWritable() {
    return manager.isWritable(this);
  }
}
