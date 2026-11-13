package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.hold.HeldItems;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.DropItem;
import com.github.mayconr.juoserver.network.packet.EquipItemRequest;
import com.github.mayconr.juoserver.network.packet.UnequipItem;

public interface ItemModule extends WorldItem, WorldModule {

    /** Renews the persistent drop date in the world's active-time clock. */
    void recordGroundInteraction(UOItem item);

    /** Active-world timestamp used by the decay flow, independent of offline time. */
    long decayNow();

    /** Puts the requested item on the player's cursor, or refuses the lift to the client. */
    void liftItem(UOPlayer player, UnequipItem request);

    /**
     * Places the held item and emits ItemDropAccepted, or restores its lift origin and emits ItemDropRejected.
     * The player's hold ends whatever the outcome.
     */
    void dropItem(UOPlayer player, DropItem dropItem);

    /** Equips the held item on the player; the player's hold ends whatever the outcome. */
    void equipHeldItem(UOPlayer player, EquipItemRequest request);

    /** Ends the player's hold without placing the item, such as when the player leaves the world. */
    void releaseHeldItem(UOPlayer player);

    /** Items currently held by players, shared by the lift, drop and equip flows. */
    HeldItems heldItems();
}
