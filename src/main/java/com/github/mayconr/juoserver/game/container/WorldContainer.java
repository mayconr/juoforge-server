package com.github.mayconr.juoserver.game.container;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.ConsumeResult;
import java.util.List;
import java.util.function.Predicate;

import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public interface WorldContainer {
    /**
     * Places an item in a loaded container and optionally merges it into an existing stack.
     * Stacking follows the current placement rules and is attempted for equipped containers.
     * Updates item location and container membership and notifies the client through the add-item flow.
     *
     * @param container destination container, on the ground, equipped or inside another container
     * @param item item to place
     * @param tryStack whether to attempt merging stackable items with the same internal name
     * @return the resulting item, which may be an existing stack
     * @throws com.github.mayconr.juoserver.game.world.ModuleOperationException if placement fails
     * @throws IllegalStateException if a referenced container item is not loaded
     */
    default UOItem addItem(UOContainer container, UOItem item, boolean tryStack) {
        return addItem(container, item, tryStack, true);
    }

    /** Adds an item through the flow, optionally suppressing the client notification.
     * @param container destination container
     * @param item item to place
     * @param tryStack whether to attempt stacking
     * @param notifyClient whether to publish the container update event
     * @return the resulting item or existing stack
     */
    UOItem addItem(UOContainer container, UOItem item, boolean tryStack, boolean notifyClient);

    /**
     * Looks up a loaded container by its serial in the realm storage cache.
     * This method does not load the container or its contents from persistence.
     *
     * @param serial container serial identifier
     * @return the loaded container, or an empty optional if no container is cached for that serial
     */
    java.util.Optional<UOContainer> getContainerBySerialId(int serial);

    /**
     * Returns the loaded direct children matching the predicate, without searching nested containers.
     * This method does not load contents from persistence.
     *
     * @param containerSerial serial of the loaded container
     * @param predicate filter applied to each direct child
     * @return matching items in a new list
     * @throws IllegalArgumentException if the container or a referenced item is not loaded
     */
    List<UOItem> getItemsInContainer(
            Integer containerSerial, Predicate<UOItem> predicate);

    /**
     * Consumes items identified by their internal name from loaded container contents.
     * Availability is checked before consumption; insufficient quantity leaves all items unchanged.
     * Partial stacks are reduced and publish an update; fully consumed stacks publish a deletion.
     * This method does not load contents from persistence.
     *
     * @param containerSerial serial of the loaded root container
     * @param name exact internal item name to match
     * @param amount quantity to consume; non-positive values perform no consumption
     * @param searchNestedContainers whether to include nested container contents
     * @return success with zero remaining, or failure with remaining equal to -1 if insufficient
     * @throws IllegalArgumentException if the container or a referenced item is not loaded
     */
    ConsumeResult consumeItem(
            Integer containerSerial, String name, int amount, boolean searchNestedContainers);

    /** Loads container contents, materializes pending corpse loot and publishes the opening event. */
    void openContainer(UOPlayer player, Container container);

    /** Total weight, including the container itself and all nested contents.
     * All contents must have been loaded before calling this method.
     */
    long getWeight(UOContainer container);

    /** Weight of all nested contents, excluding the outer container's own weight.
     * All contents must have been loaded before calling this method.
     */
    long getContentsWeight(UOContainer container);
}
