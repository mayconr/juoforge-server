package com.github.mayconr.juoserver.game.template.definitions.item;

import com.github.mayconr.juoserver.game.economy.StockType;

import java.util.List;

public interface ItemTemplateRegistry extends ItemTemplateRegistryInternal{

    ItemTemplate get(String name);

    List<ItemTemplate> get(int modelId);

    ItemTemplate getMountByNpcName(String name);

}
