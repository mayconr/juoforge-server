package com.github.mayconr.juoserver.game.session.world;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.EquipItemRequest;
import com.github.mayconr.juoserver.network.packet.UnequipItem;

public interface ItemInternal {

    void equipItem(UOPlayer player, EquipItemRequest equipItem);

    void unequipItem(UOPlayer player, UnequipItem pickedUpItem);

}
