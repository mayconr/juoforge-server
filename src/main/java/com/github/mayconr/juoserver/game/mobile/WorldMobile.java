package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.model.ResistanceType;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/** Public mobile operations, including synchronized persistent and runtime property access. */
public interface WorldMobile {
    /** Sets a property under the mobile monitor. Null removes it; runtimeOnly selects runtime storage. */
    void setProperty(UOMobile mobile, String key, Object value, boolean runtimeOnly);

    /** Reads a property under the mobile monitor, returning null when absent. */
    Object getProperty(UOMobile mobile, String key, boolean runtimeOnly);

    /** Reads a property under the mobile monitor, using the default when absent. */
    <T> T getProperty(UOMobile mobile, String key, T defaultValue, boolean runtimeOnly);

    /** Atomically computes a property under the mobile monitor; null removes it. */
    <T> T computeProperty(UOMobile mobile, String key, BiFunction<String, T, T> update, boolean runtimeOnly);

    /** Sets a primary attribute and refreshes the player status through UI. */
    void setAttribute(UOMobile mobile, MobileAttribute attribute, int value);

    /** Sets the maximum and current value of one vital and notifies clients. */
    void setVitals(UOMobile mobile, VitalType type, int max, int value);

    /** Sets one resistance within configured limits and notifies the client on every call. */
    void setResistance(UOMobile mobile, ResistanceType type, int value);

    /** Adds the item's resistances to the mobile, as when the item becomes equipped. */
    void addItemResistances(UOMobile mobile, UOItem item);

    /** Removes the item's resistances from the mobile, as when the item stops being equipped. */
    void removeItemResistances(UOMobile mobile, UOItem item);

    /** Calculates carrying capacity from the mobile's strength and gameplay settings. */
    int calculateMaxWeight(UOMobile mobile);

    /** Returns mobiles within the radius of the location that match the filter, using the spatial index. */
    List<UOMobile> getMobilesInRange(Location location, int radius, Predicate<UOMobile> filter);

    /** Runs the movement flow for a walking step in the given direction. */
    void move(UOMobile mobile, Direction direction);

    /** Runs the movement flow for a walking or running step in the given direction. */
    void move(UOMobile mobile, Direction direction, boolean running);

    /** Runs the teleport flow to relocate the mobile, rejecting destinations blocked by obstacles. */
    void teleport(UOMobile mobile, Location location);

    /**
     * Runs the teleport flow to relocate the mobile, optionally skipping the obstacle validation.
     * Permissions are not checked: callers decide who may ignore obstacles.
     *
     * @param ignoreObstacles true to accept a destination blocked by terrain or statics
     */
    void teleport(UOMobile mobile, Location location, boolean ignoreObstacles);

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
