package com.github.mayconr.juoserver.game.item.template;

import java.util.List;

public interface ItemTemplateRegistry {

    ItemTemplate get(String name);

    List<ItemTemplate> get(int modelId);

    ItemTemplate getMountByNpcName(String name);

    List<ItemTemplate> getItemTemplates(String stockType);

}
