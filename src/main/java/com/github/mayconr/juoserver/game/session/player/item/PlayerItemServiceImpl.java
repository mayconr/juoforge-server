package com.github.mayconr.juoserver.game.session.player.item;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.policy.DropItemGroundPolicy;
import com.github.mayconr.juoserver.game.policy.PolicyService;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PlayerItemServiceImpl implements PlayerItemService {
    private final UOMobile mobile;
    private final SessionFanout fanout;
    private final SessionOutbound outbound;
    private final RealmStorage storage;
    private final PolicyService policyService;

    /**
     * Adds an item to the player's inventory and notifies the client.
     */
    @Override
    public void addItemToInventory(UOItem item) {
        clearItemLocation(item);

        if (item.hasFlag(ItemFlag.STACKABLE) && tryStackItem(item)) {
            return;
        }

        addNewItemToInventory(item);
    }

    private boolean tryStackItem(UOItem item) {
        return mobile.getItemsInContainer()
                .stream()
                .filter(it -> it.getName().equals(item.getName()))
                .findFirst()
                .map(existing -> {
                    existing.increaseAmount(item.getAmount());
                    outbound.writeAndFlush(
                            new AddItemToContainer(existing.getContainer(), existing)
                    );
                    return true;
                })
                .orElse(false);
    }

    private void addNewItemToInventory(UOItem item) {
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

    @Override
    public void dropItemOnTheGround(DropItem droppedItem) {
        final var item = storage.getItemBySerialId(droppedItem.getSerialId())
                .orElseThrow(()->new IllegalStateException("Item not found " + droppedItem.getSerialId()));

        final var result = policyService.evaluate(DropItemGroundPolicy.class, new DropItemGroundPolicy(mobile, item));

        // Check policies for drop item
        if (result.allowed()) {
            clearItemLocation(item);

            item.setLocation(droppedItem);

            storage.dropItemOnTheGround(item);
        }
        fanout.writeAndFlush(new ObjectInfo(item)); // TODO filter by range
    }

    @Override
    public void dropItemInContainer(DropItem dropItem) {
        final var item = storage.getItemBySerialId(dropItem.getSerialId())
                .orElseThrow(()->new IllegalStateException("Invalid item "+dropItem.getSerialId()));

        // TODO verify if user can drop the item
        final int targetSerialId = dropItem.getContainerSerialId();

        clearItemLocation(item);

        if (UOMobile.isMobile(targetSerialId)) {
            final var mob = storage.getMobileBySerialId(targetSerialId)
                    .orElseThrow(()->new IllegalStateException("Mobile not found for serial "+targetSerialId));


            mob.addItemToContainer(item);
            item.setLocation(dropItem.getX(), dropItem.getY(), dropItem.getContainerGridIndex());

            fanout.writeAndFlush(new DeleteObject(item), out->!out.equals(outbound));
            outbound.writeAndFlush(new AddItemToContainer(mob.getBackpack(), item));
        } else if (UOItem.isItem(targetSerialId)) {
            final var targetItem = storage.getItemBySerialId(dropItem.getContainerSerialId())
                    .orElseThrow(()->new IllegalStateException("Invalid container "+dropItem.getContainerSerialId()));

            // Target item is a container
            if (targetItem instanceof UOContainer container) {

                container.addItemToContainer(item);
                item.setLocation(dropItem.getX(), dropItem.getY(), dropItem.getContainerGridIndex());

                fanout.writeAndFlush(new AddItemToContainer(container, item));

            } else {
                // Stack items
                targetItem.setAmount(targetItem.getAmount() + item.getAmount());
                storage.deleteItem(item);
                outbound.write(new DeleteObject(item));

                if (targetItem.isInContainer()) {
                    outbound.writeAndFlush(new AddItemToContainer(targetItem.getContainer(), targetItem));
                } else if (targetItem.isOnTheGround()) {
                    outbound.writeAndFlush(new ObjectInfo(targetItem));
                }
            }
        }
    }

    @Override
    public void equipItem(UOItem item, Layer layer) {
        clearItemLocation(item);

        mobile.equipItem(layer, item);

        if (log.isDebugEnabled())
            log.debug("Item [{}] equipped on layer [{}]", item.getSerialId(), layer);

        fanout.writeAndFlush(new DrawMobile(mobile)); // TODO filter by range
    }

    private void clearItemLocation(UOItem item) {
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

    @Override
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
