package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.ItemDroppedInContainer;
import com.github.mayconr.juoserver.game.model.event.ItemDroppedOnTheGround;
import com.github.mayconr.juoserver.game.model.event.ItemStacked;
import com.github.mayconr.juoserver.game.model.policy.DropItemGroundPolicy;
import com.github.mayconr.juoserver.game.world.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyService;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.DropItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ItemDropService {

    private final EventBus eventBus;
    private final RealmStorage storage;
    private final PolicyService policyService;

    private ModuleContext.ItemFacade items;

    public void dropItemOnTheGround(UOPlayer player, DropItem droppedItem) {
        final var item = storage.getItem(droppedItem.getSerialId())
                .orElseThrow(()->new IllegalStateException("Item not found " + droppedItem.getSerialId()));

        final var result = policyService.evaluate(DropItemGroundPolicy.class, new DropItemGroundPolicy(player, item));

        // Check policies for drop item
        if (result.allowed()) {
            clearItemLocation(player, item);

            item.dropOnTheGround(droppedItem);

            storage.dropItemOnTheGround(item);
        }
        eventBus.publish(new ItemDroppedOnTheGround(player, item));
    }

    public void dropItemInContainer(UOPlayer player, DropItem dropItem) {
        final int itemSerial = dropItem.getSerialId();
        final int targetSerial = dropItem.getContainerSerialId();

        final var item = storage.getItem(itemSerial)
                .orElseThrow(() -> new IllegalStateException("Invalid item " + itemSerial));

        // TODO verify if user can drop the item
        clearItemLocation(player, item);

        if (UOMobile.isMobile(targetSerial)) {
            dropIntoMobile(player, item, new DroppedItemLocation(dropItem), targetSerial);
            return;
        }

        if (UOItem.isItem(targetSerial)) {
            dropIntoOtherItem(player, item, new DroppedItemLocation(dropItem), targetSerial);
        }
    }

    public void dropItemInContainer(UOPlayer player, UOItem item, Container container) {
        final int itemSerial = item.getSerialId();
        final int targetSerial = container.getSerialId();

        // TODO verify if user can drop the item
        clearItemLocation(player, item);

        if (UOMobile.isMobile(targetSerial)) {
            dropIntoMobile(player, item, new PointInTheWorld(0,0,0), targetSerial);
            return;
        }

        if (UOItem.isItem(targetSerial)) {
            dropIntoOtherItem(player, item, new PointInTheWorld(0,0,0), targetSerial);
        }
    }

    private void dropIntoMobile(UOPlayer player, UOItem item, Location location, int targetSerial) {
        final var mobile = storage.getMobile(targetSerial)
                .orElseThrow(() -> new IllegalStateException(
                        "Mobile not found for serial " + targetSerial));

        mobile.addItemToContainer(item);
        item.setLocation(location);

        eventBus.publish(new ItemDroppedInContainer(player, mobile.getBackpack(), item));
    }

    private void dropIntoOtherItem(UOPlayer player, UOItem item, Location location, int targetSerial) {
        final var targetItem = storage.getItem(targetSerial)
                .orElseThrow(() -> new IllegalStateException("Invalid container " + targetSerial));

        if (targetItem instanceof UOContainer container) {
            container.addItemToContainer(item, location);
            eventBus.publish(new ItemDroppedInContainer(player, container, item));
            return;
        }

        stackItems(player, item, targetItem);
    }

    private void stackItems(UOPlayer player, UOItem dropped, UOItem target) {
        target.setAmount(target.getAmount() + dropped.getAmount());
        storage.deleteItem(dropped);

        if (target.isInContainer()) {
            final var container = storage.getContainer(target.getContainerSerialId())
                    .orElseThrow(()->new IllegalStateException("Invalid container " + target.getContainerSerialId()));
            eventBus.publish(new ItemStacked(player, target, dropped, ItemStacked.StackDestination.CONTAINER, container));
            return;
        }

        if (target.isOnTheGround()) {
            eventBus.publish(new ItemStacked(player, target, dropped, ItemStacked.StackDestination.GROUND, null));
        }
    }

    private void clearItemLocation(UOPlayer player, UOItem item) {
        if (item.isOnTheGround()) {
            storage.removeItemFromTheGround(item);
        }
        if (item.isEquipped()) {
            player.unequipItem(item);
        }
        if (item.isInContainer()) {
            item.removeWhenInContainer();
        }
    }

    private record DroppedItemLocation(DropItem dropItem) implements Location {

            @Override
            public int getX() {
                return dropItem.getX();
            }

            @Override
            public int getY() {
                return dropItem.getY();
            }

            @Override
            public int getZ() {
                return dropItem.getZ();
            }
        }
}
