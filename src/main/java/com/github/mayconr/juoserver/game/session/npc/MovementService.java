package com.github.mayconr.juoserver.game.session.npc;

import java.util.Collections;

import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.network.packet.DrawMobile;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MovementService {

    private final SessionFanout fanout;
    private final UONpc npc;

    public void move(Direction direction) {
        npc.move(direction);
        fanout.writeAndFlush(new DrawMobile(npc));
    }

    public void move(Location location) {
        var map = new boolean[2600][600];
        for (int x = 0; x < 2600; x++) {
            for (int y = 0; y < 600; y++) {
                map[x][y] = x >= 2512 && x <= 2518 && y >= 442 && y <= 550;
            }
        }
        try {
            final var pathfinder = new Pathfinder(map, Collections.emptySet());
            pathfinder.findNextDirection(npc, location).ifPresent(npc::move);
            fanout.writeAndFlush(new DrawMobile(npc));
        } catch (Exception exception) {
            exception.printStackTrace();
            ;
        }
    }
}
