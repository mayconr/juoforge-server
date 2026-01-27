package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.common.policy.PolicyService;
import com.github.mayconr.juoserver.common.policy.actions.DropItemGroundPolicy;
import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.world.WorldSession;
import com.github.mayconr.juoserver.network.packet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
class ItemInteractionService {
    public static final String ATTR_KEY_CAN_MOVE_ITEM = "canMoveItem";

    private final UOMobile mobile;
    private final SessionFanout fanout;
    private final SessionOutbound outbound;
    private final WorldSession worldSession;
    private final PolicyService policyService;

    public void handlePickUpItem(PickUpItem pickedUpItem) {
        doWithItem(pickedUpItem.getSerialId(), item->{
            // TODO verify distance of item and mobile
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

    public void handleDropItemOnTheGround(DropItem droppedItem) {
        doWithItem(droppedItem.getSerialId(), item -> {
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
                    worldSession.dropItemOnTheGround(item);
                }
            }
            fanout.writeAndFlush(new ObjectInfo(item)); // TODO filter by range
        });
    }

    public void handleDropItemInContainer(DropItem dropItem) {
        doWithItem(dropItem.getSerialId(), droppedItem->{
            if (isItemMovable(droppedItem)) {
                doWithContainer(dropItem.getContainerSerialId(), container->{
                    droppedItem.setLocation(dropItem.getX(), dropItem.getY(), dropItem.getContainerGridIndex());

                    // Link item to container
                    container.addItemToContainer(droppedItem);
                    droppedItem.setOwner(null);

                    fanout.writeAndFlush(new DeleteObject(droppedItem), out->!out.equals(outbound)); // TODO filter by range
                    if (mobile.equals(container) || mobile.getBackpack().equals(container)) {
                        outbound.writeAndFlush(new AddItemToContainer(mobile.getBackpack(), droppedItem));
                    } else {
                        if (container instanceof UOMobile otherMobile) {
                            fanout.writeAndFlush(new AddItemToContainer(otherMobile.getBackpack(), droppedItem)); // TODO filter by mobile container
                        } else {
                            fanout.writeAndFlush(new AddItemToContainer(container, droppedItem)); // TODO filter by range
                        }
                    }
                });
            } else {
                fanout.writeAndFlush(new ObjectInfo(droppedItem)); // TODO filter by range
            }
        });
    }

    private boolean isItemMovable(UOItem item) {
        return Boolean.TRUE.equals(item.getAndSetAttribute(ATTR_KEY_CAN_MOVE_ITEM, null));
    }

    public void handleEquipItem(UOItem item, Layer layer) {
        if (item.getContainer() != null) {
            mobile.removeItemFromContainer(item);
        } else {
            worldSession.removeItemFromTheGround(item);
        }

        mobile.equipItem(layer, item);

        if (log.isDebugEnabled())
            log.debug("Item [{}] equipped on layer [{}]", item.getSerialId(), layer);

        fanout.writeAndFlush(new DrawMobile(mobile)); // TODO filter by range
    }

    public void handleOpenContainer(Container container) {
        // TODO check if container is close enough
        outbound.write(new DrawContainer(container));
        if (!container.getItemsInContainer().isEmpty()) {
            outbound.write(new AddMultipleItemsToContainer(container, container.getItemsInContainer()));
        }
        outbound.flush();
    }

    private void doWithItem(int serialId, Consumer<UOItem> itemConsumer) {
        worldSession.findItemBySerialId(serialId)
                .thenAccept(itemConsumer)
                .whenComplete(((unused, throwable) -> logging(serialId, throwable)));
    }

    private void doWithContainer(int serialId, Consumer<Container> containerConsumer) {
        worldSession.findContainerBySerialId(serialId)
                .thenAccept(containerConsumer)
                .whenComplete(((unused, throwable) -> logging(serialId, throwable)));
    }

    private <T> void logging(int serialId, Throwable throwable) {
        if (throwable != null) {
            log.error("Unable to handle item serial [{}] due to error", serialId, throwable);
        } else {
            log.debug("Item serial [{}] not found", serialId);
        }
    }
}
