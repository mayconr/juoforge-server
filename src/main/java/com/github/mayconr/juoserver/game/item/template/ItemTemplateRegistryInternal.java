package com.github.mayconr.juoserver.game.item.template;

import com.github.mayconr.juoserver.game.economy.stock.StockType;

import java.util.List;

public interface ItemTemplateRegistryInternal {

    List<ItemTemplate> getItemTemplates(StockType stockType);
}
