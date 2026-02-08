package com.github.mayconr.juoserver.game.session.world.status;

import com.github.mayconr.juoserver.game.event.EventBus;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.StatusGumpRequested;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatusService {

    private final EventBus eventBus;
    private final RealmStorage storage;

    public void sendStatusGump(UOPlayer player, int requestedStatusSerial) {
        final var requestedMobile = storage.getMobileBySerialId(requestedStatusSerial)
                .orElseThrow(()->new IllegalArgumentException("Serial id nao encontrado "+ requestedStatusSerial));

        eventBus.publish(new StatusGumpRequested(player, requestedMobile));
    }

}
