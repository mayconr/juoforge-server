package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.DropItem;
import com.github.mayconr.juoserver.network.packet.EquipItemRequest;
import com.github.mayconr.juoserver.network.packet.UnequipItem;

public interface ItemInternal {

    void equipItem(UOPlayer player, EquipItemRequest equipItem);

    void unequipItem(UOPlayer player, UnequipItem pickedUpItem);

    void dropItemOnTheGround(UOPlayer player, DropItem dropItem);

    void removeItemFromTheGround(UOItem item);

    void dropItemInContainer(UOPlayer player, DropItem dropItem);

}
