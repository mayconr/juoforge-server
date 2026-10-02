package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.Map;

/** Mobile operations available to shard code, without client packet handlers. */
public interface WorldMobile {
    void move(UOMobile mobile, Direction direction);

    void teleport(UOMobile mobile, Location location);

    void mount(UOPlayer player, UONpc npc);

    void unmount(UOPlayer player);

    void resurrect(UOMobile mobile);

    Map<Layer, UOItem> getEquippedItems(UOMobile mobile);
}
