package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.network.packet.MoveRequest;

public interface MobileCommands {

    void mount(UOPlayer player, UONpc npc);

    void unmount(UOPlayer player);

    UONpc createNpc(String name, Location location);

    void deleteNpc(UONpc npc);

    void move(UOMobile mobile, Direction direction);

    void move(UOMobile player, MoveRequest request);

    void move(UOMobile player, Location location);
}
