package com.github.mayconr.juoserver.infrastructure.eventbus;

import org.junit.jupiter.api.Test;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class DefaultEventBusTest {
    record Signal() implements GameEvent { }

    @Test
    void supportsConcurrentPublicationAndSubscriptionChanges() {
        var bus = new DefaultEventBus();
        var received = new AtomicInteger();
        bus.register(Signal.class, event -> received.incrementAndGet());
        EventHandler<Signal> transientHandler = event -> { };
        var publication = CompletableFuture.runAsync(() -> {
            for (int i = 0; i < 1000; i++) {
                bus.publish(new Signal());
            }
        });
        var subscriptions = CompletableFuture.runAsync(() -> {
            for (int i = 0; i < 1000; i++) {
                bus.register(Signal.class, transientHandler);
                bus.unregister(Signal.class, transientHandler);
            }
        });
        CompletableFuture.allOf(publication, subscriptions).join();
        assertEquals(1000, received.get());
    }
}
