package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;

import java.util.Map;

/** Public mobile operations for movement, mounts, resurrection and equipment queries. */
public interface WorldMobile {
    /** Sets a primary attribute and refreshes the player status through UI. */
    void setAttribute(UOMobile mobile, MobileAttribute attribute, int value);

    /** Calculates carrying capacity from the mobile's strength and gameplay settings. */
    int calculateMaxWeight(UOMobile mobile);

    /** Runs the movement flow for a step in the given direction. */
    void move(UOMobile mobile, Direction direction);

    /** Runs the teleport flow to relocate the mobile. */
    void teleport(UOMobile mobile, Location location);

    /**
     * Runs the mount flow using the given NPC as the player's mount.
     * @throws ModuleOperationException if mounting fails or does not complete
     */
    void mount(UOPlayer player, UONpc npc);

    /** Runs the unmount flow for the player. */
    void unmount(UOPlayer player);

    /** Runs resurrection for a dead player, restoring initial vitals within their maximum values. */
    void resurrect(UOMobile mobile);

    /** Resolves equipped item serials into items grouped by equipment layer. */
    Map<Layer, UOItem> getEquippedItems(UOMobile mobile);
}
