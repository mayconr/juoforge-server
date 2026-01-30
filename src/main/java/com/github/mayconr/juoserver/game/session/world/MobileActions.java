package com.github.mayconr.juoserver.game.session.world;

import com.github.mayconr.juoserver.game.model.AnimationOptions;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOMobile;

public interface MobileActions {
    void move(UOMobile mobile, Direction dir);

    /**
     * Teleports the given player to the specified location in the world.
     * <p>
     * The target {@link Location} may be any valid position in the world,
     * including distant regions, different maps, or areas not currently
     * loaded in memory.
     * <p>
     * This operation bypasses normal movement validation (such as pathfinding
     * or step-based movement) and immediately relocates the player to the
     * target location. World state updates, visibility recalculation, and
     * client synchronization are handled internally by the engine.
     *
     * @param mobile   the player entity to be teleported
     * @param location the target location anywhere in the world
     */
    void teleport(UOMobile mobile, Location location);

    void deleteMobile(int serialId);

    void deleteMobile(UOMobile mobile);

    void sendAnimation(UOMobile mobile, AnimationOptions options);
}
