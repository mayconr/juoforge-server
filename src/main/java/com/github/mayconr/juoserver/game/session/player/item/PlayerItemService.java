package com.github.mayconr.juoserver.game.session.player.item;

import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.network.packet.DropItem;

public interface PlayerItemService {

    void addItemToInventory(UOItem item);

    void dropItemOnTheGround(DropItem droppedItem);

    void dropItemInContainer(DropItem dropItem);

    void equipItem(UOItem item, Layer layer);

    void openContainer(Container container);
}
