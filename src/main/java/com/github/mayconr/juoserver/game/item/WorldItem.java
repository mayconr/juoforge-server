package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.model.ItemCreationOptions;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseContext;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/** Public item operations for creation, removal, lookup and container contents. */
public interface WorldItem {

    /** Dispatches item use to the registered triggers. */
    void use(ItemUseContext context);

    /**
     * Rolls quality in [-1, 1] for an item request; does not create or modify an item.
     * Luck in [-100, 100] biases intermediate rolls; zero luck leaves the distribution unchanged.
     * Modifier zero guarantees -1 and modifier 100 guarantees +1, regardless of luck.
     * Luck and modifier outside their ranges are clamped to the nearest bound.
     *
     * @param mobile source of luck
     * @param modifier value in [0, 100], independent of any particular skill; values outside are clamped
     * @return quality suitable for {@link ItemRequest#withQuality(float)}
     * @throws IllegalArgumentException if modifier is NaN
     */
    float calculateQuality(UOMobile mobile, double modifier);

    /**
     * Creates and places an item using the default creation options, treating any rejection as an error.
     * Use {@link #tryCreateItem(ItemRequest, ItemTarget)} when a game-rule rejection is an expected outcome.
     *
     * @param request identifies the item by template, name or model ID and supplies quantity, hue and direction
     * @param target destination: ground position, container, mobile equipment or orphan state
     * @return the item produced by the creation flow
     * @throws ModuleOperationException if creation fails or is rejected, including a missing template;
     *         contains the error code, technical message, display message and original cause when available
     * @deprecated use {@link #tryCreateItem(ItemRequest, ItemTarget)} and handle the rejection explicitly
     */
    @Deprecated
    UOItem createItem(ItemRequest request, ItemTarget target);

    /**
     * Creates and places an item with customized creation options, treating any rejection as an error.
     * Use {@link #tryCreateItem(ItemRequest, ItemTarget, Consumer)} when a game-rule rejection is an expected outcome.
     *
     * @param request identifies the item by template, name or model ID and supplies quantity, hue and direction
     * @param target destination: ground position, container, mobile equipment or orphan state
     * @param options customizes creation options, such as rendering on creation; {@code null} keeps the defaults
     * @return the item produced by the creation flow
     * @throws ModuleOperationException if creation fails or is rejected, including a missing template;
     *         contains the error code, technical message, display message and original cause when available
     * @deprecated use {@link #tryCreateItem(ItemRequest, ItemTarget, Consumer)} and handle the rejection explicitly
     */
    @Deprecated
    UOItem createItem(ItemRequest request, ItemTarget target, Consumer<ItemCreationOptions> options);

    /**
     * Creates and places an item using the default creation options, reporting game-rule rejections.
     *
     * @param request identifies the item by template, name or model ID and supplies quantity, hue and direction
     * @param target destination: ground position, container, mobile equipment or orphan state
     * @return {@link ItemCreationResult.Created} with the item, or {@link ItemCreationResult.Rejected}
     *         with the {@link ItemCreationFailure} that prevented the creation
     * @throws ModuleOperationException if creation fails for a reason that is not a game-rule rejection
     */
    ItemCreationResult tryCreateItem(ItemRequest request, ItemTarget target);

    /**
     * Creates and places an item with customized creation options, reporting game-rule rejections.
     *
     * @param request identifies the item by template, name or model ID and supplies quantity, hue and direction
     * @param target destination: ground position, container, mobile equipment or orphan state
     * @param options customizes creation options, such as rendering on creation; {@code null} keeps the defaults
     * @return {@link ItemCreationResult.Created} with the item, or {@link ItemCreationResult.Rejected}
     *         with the {@link ItemCreationFailure} that prevented the creation
     * @throws ModuleOperationException if creation fails for a reason that is not a game-rule rejection
     */
    ItemCreationResult tryCreateItem(ItemRequest request, ItemTarget target, Consumer<ItemCreationOptions> options);

    void deleteItem(UOItem item);

    void deleteItem(int serial);

    Optional<UOItem> getItemBySerialId(int serial);

    /** Uses the storage visibility range; radius is currently retained for compatibility. */
    List<UOItem> getItemsInRange(Location location, int radius);

    List<ItemTemplate> getItemsTemplate(String stockType);

}
