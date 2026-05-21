package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.MoveRequest;
import com.github.mayconr.juoserver.network.packet.MoveResyncAck;
import com.github.mayconr.juoserver.network.packet.UnequipItem;

import java.util.Map;

public interface MobileModule extends WorldModule {

    void mount(UOPlayer player, UONpc npc);

    void unmount(UOPlayer player);

    void move(UOMobile mobile, Direction direction);

    void move(UOMobile mobile, MoveRequest request);

    void teleport(UOMobile mobile, Location location);

    void resync(UOPlayer player, MoveResyncAck resyncAck);

    void recalculateGold(UOMobile mobile);

    boolean equipItem(UOMobile mobile, UOItem item);

    boolean unequipItem(UOMobile mobile, UOItem item);

    boolean unequipItem(UOPlayer player, UnequipItem pickedUpItem);

    void scheduleDespawn(UONpc npc, int secs);

    void resurrect(UOMobile mobile);

    void die(DeathRequest request);

    Map<Layer, UOItem> getEquippedItems(UOMobile mobile);
}
