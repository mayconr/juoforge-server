package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.MoveRequest;
import com.github.mayconr.juoserver.network.packet.MoveResyncAck;
import com.github.mayconr.juoserver.network.packet.UnequipItem;

public interface MobileModule extends WorldMobile, WorldModule {

    void move(UOMobile mobile, MoveRequest request);

    void resync(UOPlayer player, MoveResyncAck resyncAck);

    void recalculateGold(UOMobile mobile);

    boolean equipItem(UOMobile mobile, UOItem item);

    boolean unequipItem(UOMobile mobile, UOItem item);

    boolean unequipItem(UOPlayer player, UnequipItem pickedUpItem);

    void scheduleDespawn(UONpc npc, int secs);

    void die(DeathRequest request);

}
