package com.github.mayconr.juoserver.game.session.world;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.player.target.TargetResult;

import java.util.function.Consumer;

public interface WorldActions {

    void sendBroadcastMessage(String message);

    UONpc createNpc(String name, Location location);

    void sendTarget(UOPlayer player, CursorType type, Consumer<TargetResult> consumer);

    void sendMessage(UOPlayer player, String text, MessageOptions options);

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

    void deleteItem(int serial);

    void deleteItem(UOItem item);

    void moveItem(UOItem item, Location location);

    UOItem createItemInContainer(String name, Container container);

    UOItem createItemAtLocation(String name, Location location);
}
