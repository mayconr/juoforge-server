package com.github.mayconr.juoserver.game.session.player.item;

import com.github.mayconr.juoserver.common.policy.PolicyService;
import com.github.mayconr.juoserver.common.policy.actions.DropItemGroundPolicy;
import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;
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
    private final WorldInternal worldInternal;
    private final PolicyService policyService;

    public void pickUpItem(PickUpItem pickedUpItem) {
        worldInternal.getItemBySerialId(pickedUpItem.getSerialId())
            .ifPresent(item->{
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
            });
    }

    /**
     * Adds an item to the player's inventory and notifies the client.
     */
    public void addItemToInventory(UOItem item) {
        mobile.addItemToContainer(item);

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
        worldInternal.getItemBySerialId(droppedItem.getSerialId())
            .ifPresent(item->{
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

                        worldInternal.dropItemOnTheGround(item);
                    }
                }
                fanout.writeAndFlush(new ObjectInfo(item)); // TODO filter by range
            });
    }

    public void dropItemInContainer(DropItem dropItem) {
        worldInternal.getItemBySerialId(dropItem.getSerialId())
            .ifPresent(item->{
                if (isItemMovable(item)) {
                    worldInternal.getContainerBySerialId(dropItem.getContainerSerialId())
                        .ifPresent(container->{
                            item.setLocation(dropItem.getX(), dropItem.getY(), dropItem.getContainerGridIndex());

                            // Link item to container
                            container.addItemToContainer(item);
                            item.setOwner(null);

                            fanout.writeAndFlush(new DeleteObject(item), out->!out.equals(outbound)); // TODO filter by range
                            if (mobile.equals(container) || mobile.getBackpack().equals(container)) {
                                outbound.writeAndFlush(new AddItemToContainer(mobile.getBackpack(), item));
                            } else {
                                if (container instanceof UOMobile otherMobile) {
                                    fanout.writeAndFlush(new AddItemToContainer(otherMobile.getBackpack(), item)); // TODO filter by player container
                                } else {
                                    fanout.writeAndFlush(new AddItemToContainer(container, item)); // TODO filter by range
                                }
                            }
                        });
                } else {
                    fanout.writeAndFlush(new ObjectInfo(item)); // TODO filter by range
                }
            });
    }

    private boolean isItemMovable(UOItem item) {
        return Boolean.TRUE.equals(item.getAndSetAttribute(ATTR_KEY_CAN_MOVE_ITEM, null));
    }

    public void equipItem(UOItem item, Layer layer) {
        if (item.getContainer() != null) {
            mobile.removeItemFromContainer(item);
        } else {
            worldInternal.removeItemFromTheGround(item);
        }

        mobile.equipItem(layer, item);

        if (log.isDebugEnabled())
            log.debug("Item [{}] equipped on layer [{}]", item.getSerialId(), layer);

        fanout.writeAndFlush(new DrawMobile(mobile)); // TODO filter by range
    }

    public void openContainer(Container container) {
        worldInternal.loadContainerItems(container)
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
