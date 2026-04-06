package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.flow.DamageFlowDefinition.DamageContext;
import com.github.mayconr.juoserver.game.flow.DeathFlowDefinition.DeathContext;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.ItemCreationOptions;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.UOItem;

import java.util.function.Consumer;

public interface ModuleContext {

    ItemFacade items();
    FlowFacade flows();

    interface ItemFacade {
        UOItem create(ItemRequest request, ItemTarget target, Consumer<ItemCreationOptions> options);

        UOItem create(ItemRequest request, ItemTarget target);
    }

    interface FlowFacade {
        void execute(DeathContext context);

        void execute(DamageContext context);
    }
}
