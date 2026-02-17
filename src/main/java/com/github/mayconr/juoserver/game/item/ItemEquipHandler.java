package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.ItemEquipped;
import com.github.mayconr.juoserver.game.model.event.ItemUnequipped;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.UnequipItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ItemEquipHandler {

    private final RealmStorage storage;
    private final EventBus eventBus;

    public void equipItem(UOPlayer player, UOItem item) {
        clearItemLocation(player, item);

        player.equipItem(item);

        if (log.isDebugEnabled())
            log.debug("Item [{}] equipped on layer [{}]", item.getSerialId(), item.getLayer());

        eventBus.publish(new ItemEquipped(player, item));
    }

    private void clearItemLocation(UOMobile mobile, UOItem item) {
        if (item.isOnTheGround()) {
            storage.removeItemFromTheGround(item);
        }
        if (item.isEquipped()) {
            mobile.unequipItem(item);
        }
        if (item.isInContainer()) {
            item.removeWhenInContainer();
        }
    }

    public void unequipItem(UOPlayer player, UnequipItem pickedUpItem) {
        final var item = storage.getItemBySerialId(pickedUpItem.getSerialId())
                .orElseThrow(()->new IllegalStateException("Item not found " + pickedUpItem.getSerialId()));
        // TODO verify distance of item and player
        if (item.isMovable()) {
            if (item.getContainer() != null) {
                item.getContainer().removeItemFromContainer(item);
            }
            if (player.isItemEquipped(item)) {
                player.unequipItem(item);
                eventBus.publish(new ItemUnequipped(player, item));
            }
        }
    }
}
