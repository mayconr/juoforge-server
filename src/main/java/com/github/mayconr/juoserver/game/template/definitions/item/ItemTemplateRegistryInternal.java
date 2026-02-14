package com.github.mayconr.juoserver.game.template.definitions.item;

import com.github.mayconr.juoserver.game.economy.StockType;

import java.util.List;

public interface ItemTemplateRegistryInternal {

    List<ItemTemplate> getItemTemplates(StockType stockType);
}
