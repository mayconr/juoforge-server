package com.github.mayconr.juoserver.infrastructure.gateway.client;

import com.github.mayconr.juoserver.infrastructure.gateway.protocol.*;
import io.netty.channel.Channel;
import java.util.*;
import java.util.concurrent.*;

public final class TunnelSessionManager {
  private final Map<Long, TunnelSession> sessions = new ConcurrentHashMap<>();
  private final Channel channel;
  private final TunnelSessionListener listener;
  private final TunnelSettings settings;

  TunnelSessionManager(Channel channel, TunnelSessionListener listener, TunnelSettings settings) {
    this.channel = channel;
    this.listener = listener;
    this.settings = settings;
  }

  void open(long id) {
    if (sessions.containsKey(id)) {
      channel.close();
      return;
    }
    if (sessions.size() >= settings.maxSessions()) {
      control(TunnelMessageType.SESSION_REJECT, id);
      return;
    }
    TunnelSession session = new TunnelSession(id, this);
    sessions.put(id, session);
    try {
      if (!listener.onOpen(session)) {
        sessions.remove(id);
        control(TunnelMessageType.SESSION_REJECT, id);
        return;
      }
      control(TunnelMessageType.SESSION_ACCEPT, id);
    } catch (RuntimeException e) {
      close(id, true);
    }
  }

  void data(long id, byte[] bytes) {
    TunnelSession session = sessions.get(id);
    if (session == null) {
      control(TunnelMessageType.SESSION_CLOSE, id);
      return;
    }
    try {
      listener.onData(session, bytes);
    } catch (RuntimeException e) {
      close(id, true);
    }
  }

  void close(long id, boolean notify) {
    TunnelSession session = sessions.remove(id);
    if (session == null) return;
    if (notify && channel.isActive()) control(TunnelMessageType.SESSION_CLOSE, id);
    try {
      listener.onClose(session);
    } catch (RuntimeException ignored) {
      /* cleanup must continue */
    }
  }

  void closeAll() {
    for (long id : List.copyOf(sessions.keySet())) close(id, false);
  }

  void writable() {
    for (TunnelSession session : sessions.values()) {
      try {
        listener.onWritable(session, channel.isWritable());
      } catch (RuntimeException e) {
        close(session.id(), true);
      }
    }
  }

  boolean isWritable(TunnelSession s) {
    return sessions.get(s.id()) == s && channel.isActive() && channel.isWritable();
  }

  void requestClose(TunnelSession s) {
    if (!channel.isActive()) return;
    try {
      channel.eventLoop().execute(() -> close(s.id(), true));
    } catch (RejectedExecutionException ignored) {
      /* EventLoop already stopped and sessions cleaned. */
    }
  }

  CompletionStage<Void> send(TunnelSession s, byte[] source) {
    CompletableFuture<Void> result = new CompletableFuture<>();
    if (sessions.get(s.id()) != s || !channel.isActive()) {
      return CompletableFuture.failedFuture(new IllegalStateException("Session closed"));
    }
    if (source.length > settings.bufferBytes()
        || s.pending.addAndGet(source.length) > settings.bufferBytes()) {
      if (source.length <= settings.bufferBytes()) s.pending.addAndGet(-source.length);
      requestClose(s);
      result.completeExceptionally(new IllegalStateException("Session buffer limit"));
      return result;
    }
    byte[] bytes = source.clone();
    try {
      channel
          .eventLoop()
          .execute(
              () -> {
                if (sessions.get(s.id()) != s || !channel.isActive()) {
                  s.pending.addAndGet(-bytes.length);
                  result.completeExceptionally(new IllegalStateException("Session closed"));
                  return;
                }
                // One EventLoop task per send preserves frame order even across concurrent
                // producers.
                List<CompletableFuture<Void>> writes = new ArrayList<>();
                for (int offset = 0; offset < bytes.length; ) {
                  int count = Math.min(bytes.length - offset, settings.maxFrameBytes() - 14);
                  byte[] chunk = Arrays.copyOfRange(bytes, offset, offset + count);
                  offset += count;
                  CompletableFuture<Void> write = new CompletableFuture<>();
                  writes.add(write);
                  channel
                      .writeAndFlush(new TunnelFrame(TunnelMessageType.DATA, s.id(), chunk))
                      .addListener(
                          f -> {
                            s.pending.addAndGet(-count);
                            if (f.isSuccess()) write.complete(null);
                            else {
                              write.completeExceptionally(f.cause());
                              close(s.id(), true);
                            }
                          });
                }
                CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new))
                    .whenComplete(
                        (v, e) -> {
                          if (e == null) result.complete(null);
                          else result.completeExceptionally(e);
                        });
              });
    } catch (RejectedExecutionException e) {
      s.pending.addAndGet(-bytes.length);
      result.completeExceptionally(e);
    }
    return result;
  }

  private void control(TunnelMessageType type, long id) {
    channel.writeAndFlush(TunnelFrame.control(type, id));
  }

  public int size() {
    return sessions.size();
  }
}
