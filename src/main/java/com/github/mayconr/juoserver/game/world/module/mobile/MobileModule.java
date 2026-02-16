package com.github.mayconr.juoserver.game.world.module.mobile;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.game.world.module.mobile.npc.NpcHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MobileModule implements WorldModule, MobileCommands {

    private final MountHandler mountHandler;
    private final NpcHandler npcHandler;

    @Override
    public void update(long tick, double delta) {

    }

    @Override
    public void mount(UOPlayer player, UONpc npc) {
        mountHandler.mount(player, npc);
    }

    @Override
    public void unmount(UOPlayer player) {
        mountHandler.unmount(player);
    }

    @Override
    public UONpc createNpc(String name, Location location) {
        return npcHandler.createNpc(name, location);
    }

    @Override
    public void deleteNpc(UONpc npc) {
        npcHandler.deleteNpc(npc);
    }
}
