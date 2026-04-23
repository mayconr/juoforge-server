package com.github.mayconr.juoserver.game.world.context;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.ItemCreationOptions;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractContext;

import java.util.function.Consumer;

public interface ModuleContext {

    ItemFacade items();
    FlowFacade flows();

    interface ItemFacade {
        UOItem create(ItemRequest request, ItemTarget target, Consumer<ItemCreationOptions> options);

        UOItem create(ItemRequest request, ItemTarget target);
    }

    interface FlowFacade {
        <T extends AbstractContext> void execute(T context);
    }
}
