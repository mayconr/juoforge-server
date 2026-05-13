package com.github.mayconr.juoserver.game.npc;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.WorldModule;

public interface NpcModule extends WorldModule {

    UONpc createNpc(String template, Location location);

    void removeNpc(UONpc uonpc);
}
