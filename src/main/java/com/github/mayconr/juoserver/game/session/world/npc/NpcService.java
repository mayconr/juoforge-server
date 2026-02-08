package com.github.mayconr.juoserver.game.session.world.npc;

import com.github.mayconr.juoserver.game.event.EventBus;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.event.NpcDeleted;
import com.github.mayconr.juoserver.game.model.event.NpcCreated;
import com.github.mayconr.juoserver.game.session.npc.NpcSession;
import com.github.mayconr.juoserver.game.session.npc.NpcSessionFactory;
import com.github.mayconr.juoserver.game.session.world.MobileFactory;
import com.github.mayconr.juoserver.game.session.world.SerialGenerator;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;
import com.github.mayconr.juoserver.game.session.world.item.ItemService;
import com.github.mayconr.juoserver.game.template.NpcTemplateRegistry;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class NpcService {

    private final Map<Integer, NpcSession> sessionMap = new ConcurrentHashMap<>();
    private final NpcSessionFactory npcSessionFactory;
    private final NpcTemplateRegistry npcTemplateRegistry;
    private final ItemService itemService;
    private final SerialGenerator serialGenerator;
    private final RealmStorage storage;
    private final EventBus eventBus;

    private WorldInternal worldInternal;

    public void initialize(WorldInternal worldInternal) {
        this.worldInternal = worldInternal;
    }

    public UONpc createNpc(String name, Location location) {
        final var template = npcTemplateRegistry.get(name);
        if (template == null) {
            throw new IllegalArgumentException("NPC template not found "+name);
        }
        final var npc = MobileFactory.createNpcFromTemplate(serialGenerator, template, location);

        for (String equippedItem : template.equippedItems()) {
            npc.equipItem(itemService.createEquippedItem(equippedItem, npc));
        }

        sessionMap.put(npc.getSerialId(), npcSessionFactory.create(npc, worldInternal));

        storage.cacheNpc(npc);
        eventBus.publish(new NpcCreated(npc));

        return npc;
    }

    public void deleteNpc(UONpc npc) {
        storage.deleteMobile(npc);
        eventBus.publish(new NpcDeleted(npc));
    }
}
