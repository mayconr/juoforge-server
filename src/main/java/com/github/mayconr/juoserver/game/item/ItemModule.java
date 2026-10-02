package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.DropItem;

public interface ItemModule extends WorldItem, WorldModule {
    void dropItem(UOPlayer player, DropItem dropItem);
}
