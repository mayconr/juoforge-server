package com.github.mayconr.juoserver.game.world.mount;

import com.github.mayconr.juoserver.game.event.EventBus;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.ItemEquipped;
import com.github.mayconr.juoserver.game.model.event.ItemUnequipped;
import com.github.mayconr.juoserver.game.model.policy.Mount;
import com.github.mayconr.juoserver.game.policy.PolicyService;
import com.github.mayconr.juoserver.game.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.game.world.item.ItemFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MountService {

    private final EventBus eventBus;
    private final RealmStorage storage;
    private final PolicyService policyService;
    private final ItemTemplateRegistry itemTemplateRegistry;
    private final SerialGenerator serialGenerator;

    private WorldInternal world;

    public void initialize(WorldInternal world) {
        this.world = world;
    }

    public void mount(UOPlayer player, UONpc npc) {
        if (player.getEquippedItems().get(Layer.MOUNT) != null) {
            throw new IllegalStateException("Player " + player.getName() + " already mounted");
        }
        final var result = policyService.evaluate(Mount.class, new Mount(player, npc));
        if (result.allowed()) {

            var template = itemTemplateRegistry.getMountByNpcName(npc.getName());
            if (template == null) {
                return;
            }

            final var item = ItemFactory.createFromTemplate(serialGenerator, template);

            player.equipItem(Layer.MOUNT, item);
            player.setLocation(npc);
            player.setDirection(npc.getDirection());

            world.deleteMobile(npc);

            eventBus.publish(new ItemEquipped(player, item));
        } else {
            log.info("Mount blocked by policy. Reason: {}", result.reason());
        }
    }

    public void unmount(UOPlayer player) {
        final var item = player.getEquippedItems().get(Layer.MOUNT);

        if (item == null) {
            log.debug("Player [{}] is not mounted", player.getName());
            return;
        }

        final var npcName = (String) item.get("npcName");
        if (npcName == null) {
            log.debug("Item [{}] is not a mount item", item.getName());
            return;
        }

        player.unequipItem(item);
        storage.deleteItem(item);

        final var npc = world.createNpc(npcName, player);
        npc.setDirection(player.getDirection());

        eventBus.publish(new ItemUnequipped(player, item));
    }

}
