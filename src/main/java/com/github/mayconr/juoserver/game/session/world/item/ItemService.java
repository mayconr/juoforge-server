package com.github.mayconr.juoserver.game.session.world.item;

import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.event.ItemCreated;
import com.github.mayconr.juoserver.common.event.ItemDeleted;
import com.github.mayconr.juoserver.common.event.ItemMoved;
import com.github.mayconr.juoserver.common.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.world.SerialGenerator;
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

    public UOItem handleCreateItem(String name, Location location) {
        final var template = itemTemplateRegistry.get(name);
        if (template == null) {
            throw new IllegalArgumentException("Item template ["+name+"] not found");
        }
        final var item = ItemFactory.createFromTemplate(serialGenerator, template, location);
        storage.cacheItem(item);
        drawItem(item, location);
        return item;
    }

    private void drawItem(UOItem item, Location location) {
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
    }

    public void handleDeleteItem(UOItem item) {
        storage.deleteItem(item);
        fanout.writeAndFlush(new DeleteObject(item));
        eventBus.publish(new ItemDeleted(item));
    }

    public void handleMoveItem(UOItem item, Location location) {
        item.setLocation(location);
        fanout.writeAndFlush(new ObjectInfo(item));
        eventBus.publish(new ItemMoved(item));
    }
}
