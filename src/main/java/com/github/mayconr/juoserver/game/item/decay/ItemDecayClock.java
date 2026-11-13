package com.github.mayconr.juoserver.game.item.decay;

import com.github.mayconr.juoserver.infrastructure.storage.property.Properties;
import com.github.mayconr.juoserver.infrastructure.storage.property.WorldKey;

/** Persisted date advancing only by real seconds consumed by the world update loop. */
public final class ItemDecayClock {
    public static final WorldKey<Long> KEY = WorldKey.of("item.decay.clock", Long.class);
    private final Properties properties;
    private Long milliseconds;
    private double fraction;

    public ItemDecayClock(Properties properties) {
        this.properties = properties;
    }

    public synchronized long now() {
        if (milliseconds == null) {
            milliseconds = properties.get(KEY).orElseGet(System::currentTimeMillis);
            properties.put(KEY, milliseconds);
        }
        return milliseconds;
    }

    public synchronized void advance(double seconds) {
        if (!Double.isFinite(seconds) || seconds < 0) {
            throw new IllegalArgumentException("Decay delta must be finite and nonnegative");
        }
        double elapsed = fraction + seconds * 1000.0;
        long whole = (long) Math.floor(elapsed + 1e-7);
        fraction = elapsed - whole;
        milliseconds = Math.addExact(now(), whole);
        properties.put(KEY, milliseconds);
    }
}
