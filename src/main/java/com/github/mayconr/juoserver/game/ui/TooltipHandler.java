package com.github.mayconr.juoserver.game.ui;

import com.github.mayconr.juoserver.game.model.TooltipSupport;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.TooltipRequested;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
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
        final var objects = new ArrayList<TooltipSupport>();
        for (int serialId : serials) {
            // Mobiles
            if (SerialGenerator.isMobile(serialId)) {
                storage.getMobileBySerialId(serialId).ifPresent(objects::add);
                continue;
            }
            // Items
            if (SerialGenerator.isItem(serialId)) {
                storage.getItemBySerialId(serialId).ifPresent(objects::add);
                continue;
            }
            // Vendor session (buy gump)
            if (SerialGenerator.isVirtual(serialId)) {
                var vendorSession = player.getVendorSession();
                if (vendorSession != null) {
                    objects.add(vendorSession.items().get(serialId));
                }
            }
        }

        if (objects.isEmpty()) {
            return;
        }
        eventBus.publish(new TooltipRequested(player, objects));
    }

}
