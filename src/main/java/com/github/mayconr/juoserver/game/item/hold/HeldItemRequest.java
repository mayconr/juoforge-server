package com.github.mayconr.juoserver.game.item.hold;

import com.github.mayconr.juoserver.game.model.UOPlayer;

/** Client request that ends a lift, such as a drop or an equip, and names the item it places. */
public interface HeldItemRequest {

    /** Player that sent the request. */
    UOPlayer getPlayer();

    /** Serial of the item the request places. */
    int requestedItemSerial();
}
