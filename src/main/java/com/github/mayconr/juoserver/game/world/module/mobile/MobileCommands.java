package com.github.mayconr.juoserver.game.world.module.mobile;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public interface MobileCommands {

    void mount(UOPlayer player, UONpc npc);

    void unmount(UOPlayer player);

    UONpc createNpc(String name, Location location);

    void deleteNpc(UONpc npc);

}
