package com.github.mayconr.juoserver.game.session.world;

import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public final class SerialGenerator {

    private final AtomicInteger nextItemSerial;
    private final AtomicInteger nextMobileSerial;
    private final RealmStorage realmStorage;

    public SerialGenerator(RealmStorage realmStorage) {
        this.nextItemSerial   = new AtomicInteger();
        this.nextMobileSerial = new AtomicInteger();
        this.realmStorage = realmStorage;
    }

    public void initialize() {
        realmStorage.getNextItemSerial()
            .thenCombine(realmStorage.getNextMobileSerial(), Serials::new)
            .thenAccept(serials -> {
                nextItemSerial.set(serials.itemSerial());
                nextMobileSerial.set(serials.mobileSerial());
                log.info("Serial generator initialized with values item={} mobile={}", serials.itemSerial(), serials.mobileSerial());
            })
            .whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    log.error("Unable to initialize serial generator");
                }
            });
    }

    public int nextItemSerial() {
        return nextItemSerial.getAndIncrement();
    }

    public int getCurrentItemSerial() {
        return nextItemSerial.get();
    }

    public int nextMobileMobile() {
        return nextMobileSerial.getAndIncrement();
    }

    public int getCurrentMobileSerial() {
        return nextMobileSerial.get();
    }

    private record Serials(int itemSerial, int mobileSerial) {};

}
