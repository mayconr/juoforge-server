package com.github.mayconr.juoserver.game.template;

import java.util.List;

public interface ItemTemplateRegistry {

    ItemTemplate get(String name);

    List<ItemTemplate> get(int modelId);

}
