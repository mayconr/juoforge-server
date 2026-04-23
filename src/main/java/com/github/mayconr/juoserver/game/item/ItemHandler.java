package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.event.ItemDeleted;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ItemHandler {

    private final SerialGenerator serialGenerator;
    private final ItemTemplateRegistry itemTemplateRegistry;
    private final RealmStorage storage;
    private final EventBus eventBus;

    public void deleteItem(UOItem item) {
        storage.deleteItem(item);
        eventBus.publish(new ItemDeleted(item));
    }

}
