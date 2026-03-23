package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.mobile.npc.template.NpcTemplate;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.MoveRequest;
import com.github.mayconr.juoserver.network.packet.UnequipItem;

public interface MobileModule extends WorldModule {

    void mount(UOPlayer player, UONpc npc);

    void unmount(UOPlayer player);

    UONpc createNpc(NpcTemplate template, Location location);

    void removeNpc(UONpc npc);

    void move(UOMobile mobile, Direction direction);

    void move(UOMobile player, MoveRequest request);

    void move(UOMobile player, Location location);

    void recalculateGold(UOMobile mobile);

    void equipItem(UOMobile mobile, UOItem item);

    void unequipItem(UOPlayer player, UnequipItem pickedUpItem);

    void scheduleDespawn(UONpc npc, int secs);

    void resurrect(UOMobile mobile);
}
