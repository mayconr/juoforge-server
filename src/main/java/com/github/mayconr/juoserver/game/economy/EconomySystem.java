package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.template.definitions.item.ItemTemplate;

public interface EconomySystem extends EconomySystemInternal {

    void recordProduction(String regionName, ItemTemplate itemTemplate, int amount);

    void recordConsumption(String regionName, ItemTemplate itemTemplate, int amount);

}
