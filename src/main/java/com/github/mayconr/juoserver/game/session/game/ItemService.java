package com.github.mayconr.juoserver.game.session.game;

import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.event.ItemCreated;
import com.github.mayconr.juoserver.common.event.ItemDeleted;
import com.github.mayconr.juoserver.common.event.ItemMoved;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.network.packet.DeleteObject;
import com.github.mayconr.juoserver.network.packet.ObjectInfo;
import com.github.mayconr.juoserver.network.packet.ObjectRevision;
import com.github.mayconr.juoserver.game.world.WorldService;

import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ItemService {

    private final WorldService worldService;
    private final ChannelGroup channelGroup;
    private final EventBus eventBus;

    public UOItem handleCreateItemAtLocation(String name, Location location) {
        final var item = worldService.createItemOnTheGround(name, location);
        updateItem(item, location);
        return item;
    }

    private void updateItem(UOItem item, Location location) {
        channelGroup.write(new ObjectInfo(item));
        channelGroup.write(new ObjectRevision(item));
        channelGroup.flush();
        eventBus.publish(new ItemCreated(item));
        if (log.isDebugEnabled())
            log.debug(
                    "Item [{}] created a location [{},{},{}] with serialId [{}]",
                    item,
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    item.getSerialId());
    }

    public void handleDeleteItem(UOItem item) {
        worldService.deleteItem(item);
        channelGroup.writeAndFlush(new DeleteObject(item)); // TODO filter by range
        eventBus.publish(new ItemDeleted(item));
    }

    public void handleMoveItem(UOItem item, Location location) {
        item.setLocation(location);
        channelGroup.writeAndFlush(new ObjectInfo(item));
        eventBus.publish(new ItemMoved(item));
    }
}
