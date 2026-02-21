package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Slf4j
public final class SerialGenerator {

    /*
    -- MOBILE	0x00000001 - 0x40000000
    -- ITEM	0x40000000 - 0x60000000
    -- VIRTUAL	0x60000000 - 0x7FFFFFFF
    --
    --
     */
    private static final int MOBILE_MIN_SERIAL = 0x00000001;
    private static final int MOBILE_MAX_SERIAL = 0x3FFFFFFF;
    private static final int ITEM_MIN_SERIAL = 0x40000000;
    private static final int ITEM_MAX_SERIAL = 0x5FFFFFFF;
    private static final int VIRTUAL_MIN_SERIAL = 0x60000000;
    private static final int VIRTUAL_MAX_SERIAL = 0x7FFFFFFF;

    private final AtomicInteger itemCounter = new AtomicInteger();
    private final AtomicInteger mobileCounter = new AtomicInteger();
    private final AtomicInteger virtualCounter = new AtomicInteger(VIRTUAL_MIN_SERIAL);
    private final RealmStorage realmStorage;

    public void initialize() {
        realmStorage.getNextItemSerial()
            .thenCombine(realmStorage.getNextMobileSerial(), Serials::new)
            .thenAccept(serials -> {
                itemCounter.set(serials.itemSerial());
                mobileCounter.set(serials.mobileSerial());
                log.info("Serial generator initialized with values item={} player={}", serials.itemSerial(), serials.mobileSerial());
            })
            .whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    log.error("Unable to initialize serial generator");
                }
            });
    }

    private record Serials(int itemSerial, int mobileSerial) {};

    public int getNextItem() {
        return itemCounter.getAndIncrement();
    }

    public int getCurrentItem() {
        return itemCounter.get();
    }

    public int getNextMobile() {
        return mobileCounter.getAndIncrement();
    }

    public int getCurrentMobile() {
        return mobileCounter.get();
    }

    public int getNextVirtual() {
        int nextVirtual = virtualCounter.getAndIncrement();
        if (nextVirtual >= VIRTUAL_MAX_SERIAL) {
            throw new IllegalArgumentException("Virtual maximum allowed is " + VIRTUAL_MAX_SERIAL);
        }
        return nextVirtual;
    }

    public int getCurrentVirtual() {
        return virtualCounter.get();
    }

    public static boolean isVirtual(int serial) {
        return serial >= VIRTUAL_MIN_SERIAL && serial < VIRTUAL_MAX_SERIAL;
    }

    public static boolean isMobile(int serialId) {
        return serialId <= MOBILE_MAX_SERIAL && serialId >= MOBILE_MIN_SERIAL;
    }

    public static boolean isItem(int serialId) {
        return serialId >= ITEM_MIN_SERIAL && serialId <= ITEM_MAX_SERIAL;
    }
}
