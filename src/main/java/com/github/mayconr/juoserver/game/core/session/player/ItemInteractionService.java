package com.github.mayconr.juoserver.game.core.session.player;

import com.github.mayconr.juoserver.game.core.database.Database;
import com.github.mayconr.juoserver.game.core.model.*;
import com.github.mayconr.juoserver.game.packet.*;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class ItemInteractionService {
    public static final String ATTR_KEY_CAN_MOVE_ITEM = "canMoveItem";

    private final UOMobile mobile;
    private final ChannelGroup channelGroup;
    private final ChannelHandlerContext ctx;
    private final Database database;

    public void handlePickUpItem(PickUpItem pickedUpItem) {
        final var item =
                database.getItemBySerialId(pickedUpItem.getSerialId())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Item serialId "
                                                        + pickedUpItem.getSerialId()
                                                        + " does not found!"));

        // TODO verify distance of item and mobile
        item.addAttribute(ATTR_KEY_CAN_MOVE_ITEM, item.isMovable());
        if (item.isMovable()) {
            if (item.getContainer() != null) {
                item.getContainer().removeItemFromContainer(item);
            }
            if (mobile.isItemEquipped(item)) {
                mobile.unequipItem(item);
                channelGroup.writeAndFlush(new DrawMobile(mobile)); // TODO filter by range
            }
        }
    }

    public void handleDropItemOnTheGround(DropItem droppedItem) {
        final var item =
                database.getItemBySerialId(droppedItem.getSerialId())
                        .orElseThrow(() -> new ItemNotFoundException(droppedItem.getSerialId()));

        if (isItemMovable(item)) {
            item.setLocation(droppedItem);
            database.dropItemOnTheGround(item);
        }
        channelGroup.write(new ObjectInfo(item)); // TODO filter by range
        channelGroup.flush();
    }

    public void handleDropItemInContainer(DropItem droppedItem) {
        final var item =
                database.getItemBySerialId(droppedItem.getSerialId())
                        .orElseThrow(() -> new ItemNotFoundException(droppedItem.getSerialId()));
        if (isItemMovable(item)) {
            final var newContainer =
                    database.getContainerById(droppedItem.getContainerSerialId())
                            .orElseThrow(
                                    () ->
                                            new ContainerNotFoundException(
                                                    droppedItem.getContainerSerialId()));

            item.setLocation(
                    droppedItem.getX(), droppedItem.getY(), droppedItem.getContainerGridIndex());
            newContainer.addItemToContainer(item);

            channelGroup.writeAndFlush(
                    new DeleteObject(item),
                    channel -> !channel.equals(ctx.channel())); // TODO filter by range
            if (mobile.equals(newContainer) || mobile.getBackpack().equals(newContainer)) {
                ctx.writeAndFlush(new AddItemToContainer(mobile.getBackpack(), item));
            } else {
                if (newContainer instanceof UOMobile otherMobile) {
                    channelGroup.writeAndFlush(
                            new AddItemToContainer(
                                    otherMobile.getBackpack(),
                                    item)); // TODO filter by mobile container
                } else {
                    channelGroup.writeAndFlush(
                            new AddItemToContainer(newContainer, item)); // TODO filter by range
                }
            }
        } else {
            channelGroup.write(new ObjectInfo(item)); // TODO filter by range
            channelGroup.flush();
        }
    }

    private boolean isItemMovable(UOItem item) {
        return Boolean.TRUE.equals(item.getAndSetAttribute(ATTR_KEY_CAN_MOVE_ITEM, null));
    }

    public void handleEquipItem(EquipItemRequest equipItem) {
        final var item =
                database.getItemBySerialId(equipItem.getItemSerialId())
                        .orElseThrow(() -> new ItemNotFoundException(equipItem.getItemSerialId()));

        if (item.getContainer() != null) {
            mobile.removeItemFromContainer(item);
        } else {
            database.removeItemFromTheGround(item);
        }

        mobile.equipItem(equipItem.getLayer(), item);

        if (log.isDebugEnabled())
            log.debug("Item [{}] equipped on layer [{}]", item.getSerialId(), equipItem.getLayer());

        channelGroup.writeAndFlush(new DrawMobile(mobile)); // TODO filter by range
    }

    public void handleOpenContainer(Container container) {
        // TODO check if container is close enough
        ctx.write(new DrawContainer(container));
        if (!container.getItemsInContainer().isEmpty()) {
            ctx.write(new AddMultipleItemsToContainer(container, container.getItemsInContainer()));
        }
        ctx.flush();
    }
}
