package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.ItemEquipped;
import com.github.mayconr.juoserver.game.model.event.ItemUnequipped;
import com.github.mayconr.juoserver.game.model.policy.Mount;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyService;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MountService implements WorldModule {

    private final EventBus eventBus;
    private final RealmStorage storage;
    private final PolicyService policyService;
    private final ItemTemplateRegistry itemTemplateRegistry;
    private ModuleContext.ItemFacade items;

    @Override
    public void initialize(ModuleContext context) {
        this.items = context.items();
    }

    public UOItem mount(UOPlayer player, UONpc npc) {
        if (items == null) {
            throw new IllegalStateException("MountHandler is not initialized");
        }
        if (player.getEquippedItems().get(Layer.MOUNT) != null) {
            throw new IllegalStateException("Player " + player.getName() + " already mounted");
        }
        final var result = policyService.evaluate(Mount.class, new Mount(player, npc));
        if (result.allowed()) {

            var template = itemTemplateRegistry.getMountByNpcName(npc.getName());
            if (template == null) {
                return null;
            }

            final var item = items.create(ItemRequest.byName(template.name()), ItemTarget.equip(player));

            player.equipItem(item);
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
        final var itemSerial = player.getEquippedItems().get(Layer.MOUNT);
        var item = storage.getItem(itemSerial).orElse(null);

        if (item == null) {
            log.debug("Player [{}] is not mounted", player.getName());
            return null;
        }

        player.unequipItem(item);
        storage.deleteItem(item);

        eventBus.publish(new ItemUnequipped(player, item));
        return item;
    }

    public interface MountItemFactory {
        UOItem create(UOPlayer player, String name);
    }
}
