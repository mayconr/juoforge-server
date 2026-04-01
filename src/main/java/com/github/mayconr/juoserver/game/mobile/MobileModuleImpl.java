package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.mobile.movement.MovementService;
import com.github.mayconr.juoserver.game.mobile.npc.NpcCreationService;
import com.github.mayconr.juoserver.game.mobile.npc.NpcDespawnService;
import com.github.mayconr.juoserver.game.mobile.npc.template.NpcTemplate;
import com.github.mayconr.juoserver.game.mobile.npc.template.NpcTemplateRegistry;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.MobileDeathEvent;
import com.github.mayconr.juoserver.game.model.event.MobileGoldChanged;
import com.github.mayconr.juoserver.game.model.event.MobileResurrectEvent;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.game.world.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.network.packet.MoveRequest;
import com.github.mayconr.juoserver.network.packet.UnequipItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class MobileModuleImpl implements MobileModule {


    private final MountService mountService;
    private final NpcCreationService npcCreationService;
    private final MovementService movementService;
    private final ItemEquipService itemEquipService;
    private final NpcDespawnService npcDespawnService;
    private final DeathService deathService;
    private final NpcTemplateRegistry npcTemplateRegistry;
    private final Wallet wallet;
    private final EventBus eventBus;

    @Override
    public void initialize(ModuleContext context) {
        mountService.initialize(context);
        deathService.initialize(context);
    }

    @Override
    public void update(double delta) {
        npcDespawnService.update(delta);
    }

    @Override
    public void mount(UOPlayer player, UONpc npc) {
        if (mountService.mount(player, npc) != null) {
            npcCreationService.deleteMobile(npc);
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

            npcCreationService.createNpc(template, player);

            if (log.isDebugEnabled()) {
                log.debug("Created mount NPC [{}]", npcName);
            }
        }
    }

    @Override
    public UONpc createNpc(NpcTemplate template, Location location) {
        return npcCreationService.createNpc(template, location);
    }

    @Override
    public void removeNpc(UONpc npc) {
        npcCreationService.deleteMobile(npc);
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
    public void equipItem(UOMobile mobile, UOItem item) {
        itemEquipService.equipItem(mobile, item);
    }

    @Override
    public void unequipItem(UOMobile mobile, UOItem item) {
        itemEquipService.unequipItem(mobile, item);
    }

    @Override
    public void unequipItem(UOPlayer player, UnequipItem pickedUpItem) {
        itemEquipService.unequipItem(player, pickedUpItem);
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
        deathService.die(request);
    }
}
