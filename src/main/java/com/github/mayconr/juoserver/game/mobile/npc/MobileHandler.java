package com.github.mayconr.juoserver.game.mobile.npc;

import com.github.mayconr.juoserver.game.mobile.npc.template.NpcTemplate;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.event.MobileDeleted;
import com.github.mayconr.juoserver.game.model.event.NpcCreated;
import com.github.mayconr.juoserver.game.world.MobileFactory;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MobileHandler {

    private final SerialGenerator serialGenerator;
    private final RealmStorage storage;
    private final EventBus eventBus;

    public UONpc createNpc(NpcTemplate template, Location location) {
        var npc = MobileFactory.createNpcFromTemplate(serialGenerator, template, location);
        storage.cacheMobile(npc);
        eventBus.publish(new NpcCreated(npc));
        return npc;
    }

    public void deleteMobile(UOMobile mobile) {
        storage.deleteMobile(mobile);
        eventBus.publish(new MobileDeleted(mobile));
    }
}
