package com.github.mayconr.juoserver.game.world.module.economy;

import com.github.mayconr.juoserver.game.world.module.item.template.ItemTemplate;

public interface EconomySystem extends EconomySystemInternal {

    void recordProduction(String regionName, ItemTemplate itemTemplate, int amount);

    void recordConsumption(String regionName, ItemTemplate itemTemplate, int amount);

}
