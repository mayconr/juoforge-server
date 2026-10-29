package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.mobile.flow.death.DeathContext;
import com.github.mayconr.juoserver.game.mobile.flow.equip.EquipItemContext;
import com.github.mayconr.juoserver.game.mobile.flow.mount.MountContext;
import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext;
import com.github.mayconr.juoserver.game.mobile.flow.resync.ResyncContext;
import com.github.mayconr.juoserver.game.mobile.flow.teleport.TeleportContext;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.UnequipItemContext;
import com.github.mayconr.juoserver.game.mobile.flow.unmount.UnmountContext;
import com.github.mayconr.juoserver.game.mobile.npc.NpcDespawnService;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.mobile.flow.resurrection.ResurrectionContext;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.MoveRequest;
import com.github.mayconr.juoserver.network.packet.MoveResyncAck;
import com.github.mayconr.juoserver.network.packet.UnequipItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class MobileModuleImpl implements MobileModule {

    private final NpcDespawnService npcDespawnService;
    private final Wallet wallet;
    private final EventBus eventBus;
    private final RealmStorage storage;
    private ModuleContext.FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Override
    public void update(double delta) {
        npcDespawnService.update(delta);
    }

    @Override
    public void mount(UOPlayer player, UONpc npc) {
        try {
            var context = new MountContext(player, npc);
            var result = flows.execute(context);
            if (result.flowFailed()) {
                throw ModuleOperationException.fromFailure("mount", result, "{mount.failed}");
            }
            if (!context.isMounted()) {
                throw new ModuleOperationException("mount", "MOUNT_INCOMPLETE",
                        result.reason() != null ? result.reason() : "Mount action did not complete",
                        result.displayMessage() != null ? result.displayMessage() : "{mount.failed}", null);
            }
        } catch (ModuleOperationException error) {
            throw error;
        } catch (RuntimeException error) {
            throw ModuleOperationException.unexpected("mount", error, "{mount.failed}");
        }
    }

    @Override
    public void unmount(UOPlayer player) {
        flows.execute(new UnmountContext(player));
    }

    @Override
    public void move(UOMobile mobile, Direction direction) {
        flows.execute(MovementContext.of(mobile, direction, false));
    }

    @Override
    public void move(UOMobile mobile, MoveRequest request) {
        flows.execute(MovementContext.of(mobile, request));
    }

    @Override
    public void teleport(UOMobile mobile, Location location) {
        flows.execute(TeleportContext.of(mobile, location));
    }

    @Override
    public void resync(UOPlayer player, MoveResyncAck resyncAck) {
        flows.execute(ResyncContext.of(player, resyncAck));
    }

    @Override
    public void recalculateGold(UOMobile mobile) {
        var result = flows.execute(new com.github.mayconr.juoserver.game.mobile.flow.gold.RecalculateGoldContext(mobile));
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("recalculateGold", result, null);
        }
    }

    @Override
    public boolean equipItem(UOMobile mobile, UOItem item) {
        var context = new EquipItemContext(mobile, item);
        var result = flows.execute(context);
        return !result.flowFailed() && context.isEquipped();
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
        flows.execute(new ResurrectionContext(mobile));
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
