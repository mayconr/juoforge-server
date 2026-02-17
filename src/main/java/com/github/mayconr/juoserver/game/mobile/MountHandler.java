package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.ItemEquipped;
import com.github.mayconr.juoserver.game.model.event.ItemUnequipped;
import com.github.mayconr.juoserver.game.model.policy.Mount;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.game.item.ItemFactory;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyService;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MountHandler {

    private final EventBus eventBus;
    private final RealmStorage storage;
    private final PolicyService policyService;
    private final ItemTemplateRegistry itemTemplateRegistry;
    private final SerialGenerator serialGenerator;

    public UOItem mount(UOPlayer player, UONpc npc) {
        if (player.getEquippedItems().get(Layer.MOUNT) != null) {
            throw new IllegalStateException("Player " + player.getName() + " already mounted");
        }
        final var result = policyService.evaluate(Mount.class, new Mount(player, npc));
        if (result.allowed()) {

            var template = itemTemplateRegistry.getMountByNpcName(npc.getName());
            if (template == null) {
                return null;
            }

            final var item = ItemFactory.createFromTemplate(serialGenerator, template);

            player.equipItem(Layer.MOUNT, item);
            player.setLocation(npc);
            player.setDirection(npc.getDirection());

            eventBus.publish(new ItemEquipped(player, item));
            return item;
        } else {
            log.info("Mount blocked by policy. Reason: {}", result.reason());
        }
        return null;
    }

    public UOItem unmount(UOPlayer player) {
        final var item = player.getEquippedItems().get(Layer.MOUNT);

        if (item == null) {
            log.debug("Player [{}] is not mounted", player.getName());
            return null;
        }

        player.unequipItem(item);
        storage.deleteItem(item);

        //final var npc = world.createNpc(npcName, player);
        //npc.setDirection(player.getDirection());

        eventBus.publish(new ItemUnequipped(player, item));
        return item;
    }

}
