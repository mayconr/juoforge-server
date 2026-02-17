package com.github.mayconr.juoserver.game.ui;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.TooltipRequested;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TooltipHandler {

    private final EventBus eventBus;
    private final RealmStorage storage;

    public void tooltipRequest(UOPlayer player, List<Integer> serials) {
        final var objects = new ArrayList<UOObject>();
        for (int serialId : serials) {
            if (UOMobile.isMobile(serialId)) {
                storage.getMobileBySerialId(serialId).ifPresent(objects::add);
            } else {
                storage.getItemBySerialId(serialId).ifPresent(objects::add);
            }
        }
        eventBus.publish(new TooltipRequested(player, objects));
    }

}
