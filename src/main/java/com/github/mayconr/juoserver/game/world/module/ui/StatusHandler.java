package com.github.mayconr.juoserver.game.world.module.ui;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.StatusGumpRequested;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatusHandler {

    private final EventBus eventBus;
    private final RealmStorage storage;

    public void sendStatusGump(UOPlayer player, int requestedStatusSerial) {
        final var requestedMobile = storage.getMobileBySerialId(requestedStatusSerial)
                .orElseThrow(()->new IllegalArgumentException("Serial id nao encontrado "+ requestedStatusSerial));

        eventBus.publish(new StatusGumpRequested(player, requestedMobile));
    }

}
