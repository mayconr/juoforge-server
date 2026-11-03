package com.github.mayconr.juoserver.infrastructure.gateway.protocol;

import java.time.Duration;

/** Shared transport settings; no Spring dependency. Sizes include the 14-byte wire header. */
public record TunnelSettings(
    int maxFrameBytes,
    int bufferBytes,
    int maxSessions,
    Duration heartbeat,
    Duration idleTimeout,
    Duration handshakeTimeout,
    Duration reconnectInitial,
    Duration reconnectMax) {
  public TunnelSettings {
    if (maxFrameBytes < 1024
        || maxFrameBytes > 16 * 1024 * 1024
        || bufferBytes < maxFrameBytes
        || maxSessions < 1) throw new IllegalArgumentException("Invalid transport limits");
    if (heartbeat.toMillis() < 1
        || idleTimeout.compareTo(heartbeat.multipliedBy(2)) < 0
        || handshakeTimeout.toMillis() < 1
        || reconnectInitial.toMillis() < 1
        || reconnectMax.compareTo(reconnectInitial) < 0)
      throw new IllegalArgumentException("Invalid timeouts");
  }

  public static TunnelSettings defaults() {
    return new TunnelSettings(
        65536,
        262144,
        4096,
        Duration.ofSeconds(10),
        Duration.ofSeconds(35),
        Duration.ofSeconds(10),
        Duration.ofMillis(250),
        Duration.ofSeconds(30));
  }
}
