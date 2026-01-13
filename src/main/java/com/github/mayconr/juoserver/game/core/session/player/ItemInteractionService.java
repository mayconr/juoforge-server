package com.github.mayconr.juoserver.game.core.session.player;

import com.github.mayconr.juoserver.game.core.model.Container;
import com.github.mayconr.juoserver.game.core.model.UOItem;
import com.github.mayconr.juoserver.game.core.model.UOMobile;
import com.github.mayconr.juoserver.game.packet.*;
import com.github.mayconr.juoserver.game.storage.WorldService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
class ItemInteractionService {
    public static final String ATTR_KEY_CAN_MOVE_ITEM = "canMoveItem";

    private final UOMobile mobile;
    private final ChannelGroup channelGroup;
    private final ChannelHandlerContext ctx;
    private final WorldService worldService;

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
                    channelGroup.writeAndFlush(new DrawMobile(mobile)); // TODO filter by range
                }
            }
        });
    }

    public void handleDropItemOnTheGround(DropItem droppedItem) {
        doWithItem(droppedItem.getSerialId(), item -> {
            if (isItemMovable(item)) {
                item.setLocation(droppedItem);
                worldService.dropItemOnTheGround(item);
            }
            channelGroup.write(new ObjectInfo(item)); // TODO filter by range
            channelGroup.flush();
        });
    }

    public void handleDropItemInContainer(DropItem dropItem) {
        doWithItem(dropItem.getSerialId(), droppedItem->{
            if (isItemMovable(droppedItem)) {
                doWithContainer(dropItem.getContainerSerialId(), container->{
                    droppedItem.setLocation(
                            dropItem.getX(), dropItem.getY(), dropItem.getContainerGridIndex());
                    container.addItemToContainer(droppedItem);

                    channelGroup.writeAndFlush(new DeleteObject(droppedItem),
                            channel -> !channel.equals(ctx.channel())); // TODO filter by range
                    if (mobile.equals(container) || mobile.getBackpack().equals(container)) {
                        ctx.writeAndFlush(new AddItemToContainer(mobile.getBackpack(), droppedItem));
                    } else {
                        if (container instanceof UOMobile otherMobile) {
                            channelGroup.writeAndFlush(
                                    new AddItemToContainer(
                                            otherMobile.getBackpack(),
                                            droppedItem)); // TODO filter by mobile container
                        } else {
                            channelGroup.writeAndFlush(
                                    new AddItemToContainer(container, droppedItem)); // TODO filter by range
                        }
                    }
                });
            } else {
                channelGroup.write(new ObjectInfo(droppedItem)); // TODO filter by range
                channelGroup.flush();
            }
        });
    }

    private boolean isItemMovable(UOItem item) {
        return Boolean.TRUE.equals(item.getAndSetAttribute(ATTR_KEY_CAN_MOVE_ITEM, null));
    }

    public void handleEquipItem(EquipItemRequest equipItem) {
        doWithItem(equipItem.getItemSerialId(), item->{
            if (item.getContainer() != null) {
                mobile.removeItemFromContainer(item);
            } else {
                worldService.removeItemFromTheGround(item);
            }

            mobile.equipItem(equipItem.getLayer(), item);

            if (log.isDebugEnabled())
                log.debug("Item [{}] equipped on layer [{}]", item.getSerialId(), equipItem.getLayer());

            channelGroup.writeAndFlush(new DrawMobile(mobile)); // TODO filter by range
        });
    }

    public void handleOpenContainer(Container container) {
        // TODO check if container is close enough
        ctx.write(new DrawContainer(container));
        if (!container.getItemsInContainer().isEmpty()) {
            ctx.write(new AddMultipleItemsToContainer(container, container.getItemsInContainer()));
        }
        ctx.flush();
    }

    private void doWithItem(int serialId, Consumer<UOItem> itemConsumer) {
        worldService.findItemBySerialId(serialId)
                .thenAccept(opt->opt.ifPresent(itemConsumer))
                .whenComplete(((unused, throwable) -> logging(serialId, throwable)));
    }

    private void doWithContainer(int serialId, Consumer<Container> containerConsumer) {
        worldService.findContainerBySerialId(serialId)
                .thenAccept(opt->opt.ifPresent(containerConsumer))
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
