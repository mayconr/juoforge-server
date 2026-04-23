package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.flow.DeathFlowDefinition.DeathContext;
import com.github.mayconr.juoserver.game.flow.EquipItemFlowDefinition;
import com.github.mayconr.juoserver.game.flow.UnequipItemFlowDefinition.UnequipItemContext;
import com.github.mayconr.juoserver.game.mobile.movement.MovementService;
import com.github.mayconr.juoserver.game.mobile.npc.NpcDespawnService;
import com.github.mayconr.juoserver.game.mobile.npc.template.NpcTemplateRegistry;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.MobileGoldChanged;
import com.github.mayconr.juoserver.game.model.event.MobileResurrectEvent;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.MoveRequest;
import com.github.mayconr.juoserver.network.packet.UnequipItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class MobileModuleImpl implements MobileModule {

    private final MountService mountService;
    private final MovementService movementService;
    private final NpcDespawnService npcDespawnService;
    private final NpcTemplateRegistry npcTemplateRegistry;
    private final Wallet wallet;
    private final EventBus eventBus;
    private final RealmStorage storage;

    private ModuleContext.FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();

        mountService.initialize(context);
    }

    @Override
    public void update(double delta) {
        npcDespawnService.update(delta);
    }

    @Override
    public void mount(UOPlayer player, UONpc npc) {
        if (mountService.mount(player, npc) != null) {
            //npcCreationService.deleteMobile(npc);
        }
    }

    @Override
    public void unmount(UOPlayer player) {
        var item = mountService.unmount(player);
        if (item != null) {
            final var npcName = (String) item.persistentAttributes().get("npcName");
            if (npcName == null) {
                log.debug("Item [{}] is not a mount item", item.getName());
                return;
            }
            final var template = npcTemplateRegistry.get(npcName);
            if (template == null) {
                throw  new IllegalStateException("NPC [" + npcName + "] is not a mount item");
            }

            //npcCreationService.createNpc(template, player);

            if (log.isDebugEnabled()) {
                log.debug("Created mount NPC [{}]", npcName);
            }
        }
    }

    @Override
    public void move(UOMobile mobile, Direction direction) {
        movementService.move(mobile, direction);
    }

    @Override
    public void move(UOMobile mobile, MoveRequest request) {
        movementService.move(mobile, request);
    }

    @Override
    public void move(UOMobile mobile, Location location) {
        movementService.move(mobile, location);
    }

    @Override
    public void recalculateGold(UOMobile mobile) {
        int oldBalance = mobile.getGold();
        mobile.setGold(wallet.getBalance(mobile));
        eventBus.publish(new MobileGoldChanged(mobile, oldBalance, mobile.getGold()));
    }

    @Override
    public boolean equipItem(UOMobile mobile, UOItem item) {
        var context = new EquipItemFlowDefinition.EquipItemContext(mobile, item);
        flows.execute(context);
        //itemEquipService.equipItem(mobile, item);
        return context.isEquipped();
    }

    @Override
    public boolean unequipItem(UOMobile mobile, UOItem item) {
        var context = new UnequipItemContext(mobile, item);
        flows.execute(context);
        return context.isUnequipped();
    }

    @Override
    public boolean unequipItem(UOPlayer player, UnequipItem pickedUpItem) {
        var context = new UnequipItemContext(player, pickedUpItem);
        flows.execute(context);
        return context.isUnequipped();
    }

    @Override
    public void scheduleDespawn(UONpc npc, int secs) {
        npcDespawnService.scheduleDespawn(npc, secs);
    }

    @Override
    public void resurrect(UOMobile mobile) {
        mobile.setMana(5);
        mobile.setStamina(5);
        mobile.setHitpoints(5);
        mobile.setAlive(true);

        eventBus.publish(new MobileResurrectEvent(mobile));
    }

    @Override
    public void die(DeathRequest request) {
        flows.execute(new DeathContext(request.victim(), request.killer(), request.cause()));
    }

    @Override
    public Map<Layer, UOItem> getEquippedItems(UOMobile mobile) {
        final var equippedSerials = mobile.getEquippedItems();
        final Map<Layer, UOItem> equippedItems = new HashMap<>(equippedSerials.size());
        for (Map.Entry<Layer, Integer> entry : equippedSerials.entrySet()) {
            var itemSerial = entry.getValue();

            var item = storage.getItem(itemSerial)
                    .orElseThrow(() -> new IllegalStateException("Cannot find equipped Item ["+itemSerial+"] for player " + mobile.getName()));
            equippedItems.put(entry.getKey(), item);
        }
        return equippedItems;
    }
}
