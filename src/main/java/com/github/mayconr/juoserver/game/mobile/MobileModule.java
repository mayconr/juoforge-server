package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.MoveRequest;
import com.github.mayconr.juoserver.network.packet.MoveResyncAck;
import com.github.mayconr.juoserver.network.packet.UnequipItem;

/** Internal mobile module with lifecycle, client requests and state management. */
public interface MobileModule extends WorldMobile, WorldModule {

    /** Runs the movement flow for a client request. */
    void move(UOMobile mobile, MoveRequest request);

    /** Runs the movement resynchronization flow for the player. */
    void resync(UOPlayer player, MoveResyncAck resyncAck);

    /** Refreshes the mobile's gold balance from its wallet and publishes the change. */
    void recalculateGold(UOMobile mobile);

    /** Runs the equip flow and returns whether the item was equipped. */
    boolean equipItem(UOMobile mobile, UOItem item);

    /** Runs the unequip flow and returns whether the item was unequipped. */
    boolean unequipItem(UOMobile mobile, UOItem item);

    /** Handles a client pickup request through the unequip flow. */
    boolean unequipItem(UOPlayer player, UnequipItem pickedUpItem);

    /** Schedules NPC despawn after the given delay in seconds. */
    void scheduleDespawn(UONpc npc, int secs);

    /** Runs the death flow with the victim, killer and cause from the request. */
    void die(DeathRequest request);

}
