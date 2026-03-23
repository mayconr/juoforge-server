package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.ItemCreationOptions;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UONpc;

import java.util.function.Consumer;

public interface ModuleContext {

    ItemFacade items();

    interface ItemFacade {
        UOItem create(ItemRequest request, ItemTarget target, Consumer<ItemCreationOptions> options);

        UOItem create(ItemRequest request, ItemTarget target);
    }

}
