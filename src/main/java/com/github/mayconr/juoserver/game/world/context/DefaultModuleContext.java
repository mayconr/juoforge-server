package com.github.mayconr.juoserver.game.world.context;

import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.ItemCreationOptions;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.UOItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class DefaultModuleContext implements ModuleContext {

    private final ItemModule itemModule;
    private final FlowFacade flowFacade;

    @Override
    public ItemFacade items() {
        return new ItemFacade() {
            @Override
            public UOItem create(ItemRequest request, ItemTarget target, Consumer<ItemCreationOptions> options) {
                return itemModule.createItem(request, target, options);
            }

            @Override
            public UOItem create(ItemRequest request, ItemTarget target) {
                return itemModule.createItem(request, target);
            }

        };
    }

    @Override
    public FlowFacade flows() {
        return flowFacade;
    }
}
