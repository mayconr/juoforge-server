package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseContext;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Public item operations for creation, removal, lookup and container contents. */
public interface WorldItem {

    /** Dispatches item use to the registered triggers. */
    void use(ItemUseContext context);

    /** Loads container contents, materializes pending corpse loot and publishes the opening event. */
    void openContainer(UOPlayer player, Container container);

    /**
     * Creates and places an item using the default creation options.
     *
     * @param request identifies the item by template, name or model ID and supplies quantity, hue and direction
     * @param target destination: ground position, container, mobile equipment or orphan state
     * @return the item produced by the creation flow
     * @throws ModuleOperationException if creation fails, including a missing template;
     *         contains the error code, technical message, display message and original cause when available
     */
    UOItem createItem(ItemRequest request, ItemTarget target);

    /**
     * Creates and places an item with customized creation options.
     *
     * @param request identifies the item by template, name or model ID and supplies quantity, hue and direction
     * @param target destination: ground position, container, mobile equipment or orphan state
     * @param options customizes creation options, such as rendering on creation; {@code null} keeps the defaults
     * @return the item produced by the creation flow
     * @throws ModuleOperationException if creation fails, including a missing template;
     *         contains the error code, technical message, display message and original cause when available
     */
    UOItem createItem(ItemRequest request, ItemTarget target, Consumer<ItemCreationOptions> options);

    void deleteItem(UOItem item);

    void deleteItem(int serial);

    Optional<UOItem> getItemBySerialId(int serial);

    Optional<UOContainer> getContainerBySerialId(int serial);

    /** Uses the storage visibility range; radius is currently retained for compatibility. */
    List<UOItem> getItemsInRange(Location location, int radius);

    List<ItemTemplate> getItemsTemplate(String stockType);

    ConsumeResult consumeItem(Integer container, String name, int amount, boolean searchNestedContainers);

    List<UOItem> getItemsInContainer(Integer containerSerial, Predicate<UOItem> predicate);
}
