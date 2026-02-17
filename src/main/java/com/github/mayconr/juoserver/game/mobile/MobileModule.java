package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.mobile.movement.MovementHandler;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.game.mobile.npc.NpcHandler;
import com.github.mayconr.juoserver.network.packet.MoveRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MobileModule implements WorldModule, MobileCommands {

    private final MountHandler mountHandler;
    private final NpcHandler npcHandler;
    private final MovementHandler movementHandler;

    @Override
    public void update(double delta) {

    }

    @Override
    public void mount(UOPlayer player, UONpc npc) {
        if (mountHandler.mount(player, npc) != null) {
            npcHandler.deleteNpc(npc);
        }
    }

    @Override
    public void unmount(UOPlayer player) {
        var item = mountHandler.unmount(player);
        if (item != null) {
            final var npcName = (String) item.getPersistentAttribute("npcName");
            if (npcName == null) {
                log.debug("Item [{}] is not a mount item", item.getName());
                return;
            }

            npcHandler.createNpc(npcName, player, npc->{
                npc.setDirection(player.getDirection());
            });
        }
    }

    @Override
    public UONpc createNpc(String name, Location location) {
        return npcHandler.createNpc(name, location, npc->{});
    }

    @Override
    public void deleteNpc(UONpc npc) {
        npcHandler.deleteNpc(npc);
    }

    @Override
    public void move(UOMobile mobile, Direction direction) {
        movementHandler.move(mobile, direction);
    }

    @Override
    public void move(UOMobile player, MoveRequest request) {
        movementHandler.move(player, request);
    }

    @Override
    public void move(UOMobile player, Location location) {
        movementHandler.move(player, location);
    }
}
