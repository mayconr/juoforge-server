package com.github.mayconr.juoserver.infrastructure.gateway.client;

/**
 * Callbacks run on the tunnel EventLoop and must never block. Data arrays belong to the callback.
 */
public interface TunnelSessionListener {
  boolean onOpen(TunnelSession session);

  void onData(TunnelSession session, byte[] data);

  default void onClose(TunnelSession session) {}

  /** Pause/resume the producer (e.g. local channel AUTO_READ). */
  default void onWritable(TunnelSession session, boolean writable) {}
}
