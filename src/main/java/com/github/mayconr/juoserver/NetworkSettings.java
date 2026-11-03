package com.github.mayconr.juoserver;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

/** Transport configuration, deliberately separate from gameplay settings. */
public record NetworkSettings(Mode mode, String host, int port, String shard, String token) {
    public enum Mode { STANDALONE, GATEWAY }

    public NetworkSettings {
        Objects.requireNonNull(mode, "Network mode is required");
        if (host == null || host.isBlank()) throw new IllegalArgumentException("Network host is required");
        if (port < 1 || port > 65535) throw new IllegalArgumentException("Invalid network port");
        if (mode == Mode.GATEWAY && (shard == null || shard.isBlank() || shard.indexOf('\0') >= 0
                || token == null || token.isBlank()
                || (shard + '\0' + token).getBytes(StandardCharsets.UTF_8).length > 4096)) {
            throw new IllegalArgumentException("Valid gateway shard and token are required");
        }
    }

    public static NetworkSettings standalone(String host, int port) {
        return new NetworkSettings(Mode.STANDALONE, host, port, null, null);
    }

    public static NetworkSettings gateway(String host, int port, String shard, String token) {
        return new NetworkSettings(Mode.GATEWAY, host, port, shard, token);
    }

    public static NetworkSettings from(Properties properties) {
        String configuredMode = properties.getProperty("network.mode");
        if (configuredMode == null || configuredMode.isBlank()) {
            throw new IllegalArgumentException("network.mode is required (standalone or gateway)");
        }
        Mode mode = Mode.valueOf(configuredMode.trim().toUpperCase(java.util.Locale.ROOT));
        return new NetworkSettings(mode, properties.getProperty("network.host", mode == Mode.GATEWAY ? "127.0.0.1" : "0.0.0.0"),
                Integer.parseInt(properties.getProperty("network.port", mode == Mode.GATEWAY ? "2594" : "2593")),
                properties.getProperty("network.gateway.shard", "default"), properties.getProperty("network.gateway.token"));
    }

    @Override
    public String toString() {
        return "NetworkSettings[mode=" + mode + ", host=" + host + ", port=" + port + ", shard=" + shard + ", token=<redacted>]";
    }
}
