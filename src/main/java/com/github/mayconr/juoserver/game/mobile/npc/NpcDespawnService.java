package com.github.mayconr.juoserver.game.mobile.npc;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class NpcDespawnService implements WorldModule {

    private final RealmStorage storage;
    private final Map<Integer, DespawnEntry> scheduled = new HashMap<>();

    @Override
    public void update(double delta) {
        Iterator<Map.Entry<Integer, DespawnEntry>> iterator = scheduled.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, DespawnEntry> entry = iterator.next();
            DespawnEntry despawnEntry = entry.getValue();

            despawnEntry.remainingSeconds -= delta;

            if (despawnEntry.remainingSeconds <= 0) {
                UONpc npc = despawnEntry.npc;

                if (!npc.isAlive()) {
                    log.info("Npc {} has been destroyed", npc.getId());
                    for (UOItem equippedItem : npc.getEquippedItems().values()) {
                        storage.deleteItem(equippedItem);
                    }
                    for (UOItem containerItem : npc.getContainerItems()) {
                        storage.deleteItem(containerItem);
                    }
                    storage.deleteMobile(npc);
                }

                iterator.remove();
            }
        }
    }

    public void scheduleDespawn(UONpc npc, int secs) {
        scheduled.put(npc.getSerialId(), new DespawnEntry(npc, secs));
    }

    @AllArgsConstructor
    private static final class DespawnEntry {
        private final UONpc npc;
        private double remainingSeconds;
    }
}
