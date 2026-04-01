package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.ItemCreationOptions;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.UOItem;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class DefaultModuleContext implements ModuleContext {

    private final ItemModule itemModule;

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

}
