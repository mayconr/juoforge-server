package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.mobile.movement.MovementHandler;
import com.github.mayconr.juoserver.game.mobile.npc.MobileHandler;
import com.github.mayconr.juoserver.game.mobile.npc.template.NpcTemplate;
import com.github.mayconr.juoserver.game.mobile.npc.template.NpcTemplateRegistry;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.MobileGoldChanged;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.network.packet.MoveRequest;
import com.github.mayconr.juoserver.network.packet.UnequipItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MobileModule implements WorldModule, MobileCommands, MobileQueries {

    private final MountHandler mountHandler;
    private final MobileHandler mobileHandler;
    private final MovementHandler movementHandler;
    private final ItemEquipHandler itemEquipHandler;
    private final NpcTemplateRegistry npcTemplateRegistry;
    private final Wallet wallet;
    private final EventBus eventBus;

    public void initialize(MountHandler.MountItemFactory mountItemFactory) {
        mountHandler.initialize(mountItemFactory);
    }

    @Override
    public void update(double delta) {

    }

    @Override
    public void mount(UOPlayer player, UONpc npc) {
        if (mountHandler.mount(player, npc) != null) {
            mobileHandler.deleteMobile(npc);
        }
    }

    @Override
    public void unmount(UOPlayer player) {
        var item = mountHandler.unmount(player);
        if (item != null) {
            final var npcName = (String) item.getPersistentAttribute("npcName");
            if (npcName == null) {
                log.debug("Item [{}] is not a mount item", item.getName());
                return;
            }
            final var template = npcTemplateRegistry.get(npcName);
            if (template == null) {
                throw  new IllegalStateException("NPC [" + npcName + "] is not a mount item");
            }

            mobileHandler.createNpc(template, player);

            if (log.isDebugEnabled()) {
                log.debug("Created mount NPC [{}]", npcName);
            }
        }
    }

    @Override
    public UONpc createNpc(NpcTemplate template, Location location) {
        return mobileHandler.createNpc(template, location);
    }

    @Override
    public void removeNpc(UONpc npc) {
        mobileHandler.deleteMobile(npc);
    }

    @Override
    public void move(UOMobile mobile, Direction direction) {
        movementHandler.move(mobile, direction);
    }

    @Override
    public void move(UOMobile mobile, MoveRequest request) {
        movementHandler.move(mobile, request);
    }

    @Override
    public void move(UOMobile mobile, Location location) {
        movementHandler.move(mobile, location);
    }

    @Override
    public void recalculateGold(UOMobile mobile) {
        int oldBalance = mobile.getGold();
        mobile.setGold(wallet.getBalance(mobile));
        eventBus.publish(new MobileGoldChanged(mobile, oldBalance, mobile.getGold()));
    }

    @Override
    public void equipItem(UOMobile mobile, UOItem item) {
        itemEquipHandler.equipItem(mobile, item);
    }

    @Override
    public void unequipItem(UOPlayer player, UnequipItem pickedUpItem) {
        itemEquipHandler.unequipItem(player, pickedUpItem);
    }
}
