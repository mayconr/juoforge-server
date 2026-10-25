package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.Map;

/** Public mobile operations for movement, mounts, resurrection and equipment queries. */
public interface WorldMobile {
    /** Runs the movement flow for a step in the given direction. */
    void move(UOMobile mobile, Direction direction);

    /** Runs the teleport flow to relocate the mobile. */
    void teleport(UOMobile mobile, Location location);

    /** Runs the mount flow using the given NPC as the player's mount. */
    void mount(UOPlayer player, UONpc npc);

    /** Runs the unmount flow for the player. */
    void unmount(UOPlayer player);

    /** Restores life with initial vitals and publishes a resurrection event. */
    void resurrect(UOMobile mobile);

    /** Resolves equipped item serials into items grouped by equipment layer. */
    Map<Layer, UOItem> getEquippedItems(UOMobile mobile);
}
