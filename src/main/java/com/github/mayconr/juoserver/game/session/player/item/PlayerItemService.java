package com.github.mayconr.juoserver.game.session.player.item;

import com.github.mayconr.juoserver.common.policy.PolicyService;
import com.github.mayconr.juoserver.common.policy.actions.DropItemGroundPolicy;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PlayerItemService {
    public static final String ATTR_KEY_CAN_MOVE_ITEM = "canMoveItem";

    private final UOMobile mobile;
    private final SessionFanout fanout;
    private final SessionOutbound outbound;
    private final RealmStorage storage;
    private final PolicyService policyService;

    public void pickUpItem(PickUpItem pickedUpItem) {
        final var item = storage.getItemBySerialId(pickedUpItem.getSerialId())
                        .orElseThrow(()->new IllegalStateException("Item not found " + pickedUpItem.getSerialId()));
        // TODO verify distance of item and player
        item.addAttribute(ATTR_KEY_CAN_MOVE_ITEM, item.isMovable());
        if (item.isMovable()) {
            if (item.getContainer() != null) {
                item.getContainer().removeItemFromContainer(item);
            }
            if (mobile.isItemEquipped(item)) {
                mobile.unequipItem(item);
                fanout.writeAndFlush(new DrawMobile(mobile)); // TODO filter by range
            }
        }
    }

    /**
     * Adds an item to the player's inventory and notifies the client.
     */
    public void addItemToInventory(UOItem item) {
        mobile.addItemToContainer(item);
        storage.removeItemFromTheGround(item);

        outbound.writeAndFlush(new AddItemToContainer(mobile, item));

        if (log.isDebugEnabled()) {
            log.debug(
                    "Item [{}-{}] added to container [{}-{}]",
                    item.getSerialId(),
                    item.getName(),
                    mobile.getSerialId(),
                    mobile.getName());
        }
    }

    public void dropItemOnTheGround(DropItem droppedItem) {
        final var item = storage.getItemBySerialId(droppedItem.getSerialId())
                .orElseThrow(()->new IllegalStateException("Item not found " + droppedItem.getSerialId()));

        // TODO verify if user can drop the item

        if (isItemMovable(item)) {
            final var result = policyService.evaluate(DropItemGroundPolicy.class, new DropItemGroundPolicy(mobile, item));
            // Check policies for drop item
            if (result.allowed()) {
                item.setLocation(droppedItem);

                final var container = item.getContainer();
                if (container != null) {
                    container.removeItemFromContainer(item);
                }
                item.setOwner(null);

                storage.dropItemOnTheGround(item);
            }
        }
        fanout.writeAndFlush(new ObjectInfo(item)); // TODO filter by range
    }

    public void dropItemInContainer(DropItem dropItem) {
        final var item = storage.getItemBySerialId(dropItem.getSerialId())
                .orElseThrow(()->new IllegalStateException("Invalid item "+dropItem.getSerialId()));

        if (!isItemMovable(item)) {
            fanout.writeAndFlush(new ObjectInfo(item));
            return;
        }
        // TODO verify if user can drop the item
        final int targetSerialId = dropItem.getContainerSerialId();

        // Remove item from the ground
        storage.removeItemFromTheGround(item);

        if (UOMobile.isMobile(targetSerialId)) {
            final var mob = storage.getMobileBySerialId(targetSerialId)
                    .orElseThrow(()->new IllegalStateException("Mobile not found for serial "+targetSerialId));

            item.setLocation(dropItem.getX(), dropItem.getY(), dropItem.getContainerGridIndex());
            item.setOwner(null);
            mob.addItemToContainer(item);

            fanout.writeAndFlush(new DeleteObject(item), out->!out.equals(outbound));
            outbound.writeAndFlush(new AddItemToContainer(mob.getBackpack(), item));
        }

        if (UOItem.isItem(targetSerialId)) {
            final var targetItem = storage.getItemBySerialId(dropItem.getContainerSerialId())
                    .orElseThrow(()->new IllegalStateException("Invalid container "+dropItem.getContainerSerialId()));

            // Target item is a container
            if (targetItem instanceof UOContainer container) {
                item.setLocation(dropItem.getX(), dropItem.getY(), dropItem.getContainerGridIndex());
                item.setOwner(null);
                container.addItemToContainer(item);

                fanout.writeAndFlush(new AddItemToContainer(container, item));

            } else {
                // Stack items
                targetItem.setAmount(targetItem.getAmount() + item.getAmount());
                storage.deleteItem(item);
                outbound.write(new DeleteObject(item));

                if (targetItem.isOnBackpack()) {
                    outbound.writeAndFlush(new AddItemToContainer(targetItem.getContainer(), targetItem));
                } else if (targetItem.isOnTheGround()) {
                    outbound.writeAndFlush(new ObjectInfo(targetItem));
                }
            }
        }
    }

    private boolean isItemMovable(UOItem item) {
        return Boolean.TRUE.equals(item.getAndSetAttribute(ATTR_KEY_CAN_MOVE_ITEM, null));
    }

    public void equipItem(UOItem item, Layer layer) {
        if (item.getContainer() != null) {
            mobile.removeItemFromContainer(item);
        } else {
            storage.removeItemFromTheGround(item);
        }

        mobile.equipItem(layer, item);

        if (log.isDebugEnabled())
            log.debug("Item [{}] equipped on layer [{}]", item.getSerialId(), layer);

        fanout.writeAndFlush(new DrawMobile(mobile)); // TODO filter by range
    }

    public void openContainer(Container container) {
        storage.loadContainerItems(container)
            .thenAccept(items->{
                // TODO check container range
                container.addItemsToContainer(items);

                outbound.write(new DrawContainer(container));
                if (!container.getItemsInContainer().isEmpty()) {
                    outbound.write(new AddMultipleItemsToContainer(container, container.getItemsInContainer()));
                }
                outbound.flush();
            })
            .whenComplete(((unused, throwable) -> logging(container.getSerialId(), throwable)));
    }

    private void logging(int serialId, Throwable throwable) {
        if (throwable != null) {
            log.error("Unable to handle item serial [{}] due to error", serialId, throwable);
        } else {
            log.debug("Item serial [{}] not found", serialId);
        }
    }
}
