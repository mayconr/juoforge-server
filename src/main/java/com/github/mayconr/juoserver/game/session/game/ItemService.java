package com.github.mayconr.juoserver.game.session.game;

import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.event.ItemCreated;
import com.github.mayconr.juoserver.common.event.ItemDeleted;
import com.github.mayconr.juoserver.common.event.ItemMoved;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.network.packet.DeleteObject;
import com.github.mayconr.juoserver.network.packet.ObjectInfo;
import com.github.mayconr.juoserver.network.packet.ObjectRevision;
import com.github.mayconr.juoserver.game.world.WorldService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class ItemService {

    private final WorldService worldService;
    private final SessionFanout fanout;
    private final EventBus eventBus;

    public void initialize() {
        worldService.loadGroundItems();
    }

    public CompletableFuture<UOItem> handleCreateItemAtLocation(String name, Location location) {
        return worldService.createItemAtLocation(name, location)
            .thenApply(item->{
                updateItem(item, location);
                return item;
            });
    }

    private void updateItem(UOItem item, Location location) {
        fanout.write(new ObjectInfo(item));
        fanout.write(new ObjectRevision(item));
        fanout.flush();
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
        fanout.writeAndFlush(new DeleteObject(item)); // TODO filter by range
        eventBus.publish(new ItemDeleted(item));
    }

    public void handleMoveItem(UOItem item, Location location) {
        item.setLocation(location);
        fanout.writeAndFlush(new ObjectInfo(item));
        eventBus.publish(new ItemMoved(item));
    }
}
