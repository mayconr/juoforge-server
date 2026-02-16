package com.github.mayconr.juoserver.game.world.module.item.template;

import com.github.mayconr.juoserver.game.world.module.economy.StockType;

import java.util.List;

public interface ItemTemplateRegistryInternal {

    List<ItemTemplate> getItemTemplates(StockType stockType);
}
