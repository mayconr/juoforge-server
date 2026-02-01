package com.github.mayconr.juoserver.game.session.world.item;

import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.event.ItemCreated;
import com.github.mayconr.juoserver.common.event.ItemDeleted;
import com.github.mayconr.juoserver.common.event.ItemMoved;
import com.github.mayconr.juoserver.common.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.world.SerialGenerator;
import com.github.mayconr.juoserver.game.session.world.player.PlayerSessionService;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.DeleteObject;
import com.github.mayconr.juoserver.network.packet.ObjectInfo;
import com.github.mayconr.juoserver.network.packet.ObjectRevision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ItemService {

    private final SerialGenerator serialGenerator;
    private final ItemTemplateRegistry itemTemplateRegistry;
    private final RealmStorage storage;
    private final SessionFanout fanout;
    private final EventBus eventBus;
    private final PlayerSessionService playerSessionService;

    public UOItem createItemAtLocation(String name, Location location) {
        final var item = createAndCacheItem(name, location);

        fanout.write(new ObjectInfo(item));
        fanout.write(new ObjectRevision(item));
        fanout.flush();
        eventBus.publish(new ItemCreated(item));

        if (log.isDebugEnabled())
            log.debug(
                    "Item [{}] created a trigger [{},{},{}] with serialId [{}]",
                    item,
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    item.getSerialId());

        return item;
    }

    public UOItem createItemInContainer(String name, Container container) {
        final var item = createAndCacheItem(name, new PointInTheWorld(0,0,0));
        if (container instanceof UOPlayer player) {
            playerSessionService.getSession(player).addItemToInventory(item);
        } else {
            container.addItemToContainer(item);
        }
        return item;
    }

    private UOItem createAndCacheItem(String name, Location location) {
        final var template = itemTemplateRegistry.get(name);
        if (template == null) {
            throw new IllegalArgumentException("Item template ["+name+"] not found");
        }
        final var item = ItemFactory.createFromTemplate(serialGenerator, template, location);
        storage.cacheItem(item);
        return item;
    }

    public void deleteItem(UOItem item) {
        storage.deleteItem(item);
        fanout.writeAndFlush(new DeleteObject(item));
        eventBus.publish(new ItemDeleted(item));
    }

    public void moveItem(UOItem item, Location location) {
        item.setLocation(location);
        fanout.writeAndFlush(new ObjectInfo(item));
        eventBus.publish(new ItemMoved(item));
    }
}
