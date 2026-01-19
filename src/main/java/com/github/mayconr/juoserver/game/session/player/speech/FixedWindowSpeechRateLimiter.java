package com.github.mayconr.juoserver.game.session.player.speech;

import com.github.mayconr.juoserver.game.model.UOMobile;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class FixedWindowSpeechRateLimiter implements SpeechRateLimiter {
    private static final int MAX_MESSAGES = 5;
    private static final long WINDOW_MS = TimeUnit.SECONDS.toMillis(3);

    private final ConcurrentHashMap<Long, Window> windows = new ConcurrentHashMap<>();

    @Override
    public boolean allow(UOMobile mobile) {
        if (mobile == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        long id = mobile.getSerialId();

        Window window = windows.compute(id, (k, w) -> {
            if (w == null || now - w.windowStart >= WINDOW_MS) {
                return new Window(now, 1);
            }
            w.count++;
            return w;
        });

        return window.count <= MAX_MESSAGES;
    }

    private static final class Window {
        final long windowStart;
        int count;

        Window(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
