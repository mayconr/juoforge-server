package com.github.mayconr.juoserver.game.region;

import com.github.mayconr.juoserver.game.model.Location;

import java.util.Optional;

/**
 * Domain service responsible for managing and resolving {@link RegionNode} instances
 * within the game world.
 *
 * <p>
 * This service provides:
 * <ul>
 *     <li>Registration of regions into the current world context</li>
 *     <li>Lookup of regions by unique identifier</li>
 *     <li>Resolution of the most specific region for a given {@link Location}</li>
 * </ul>
 *
 * <p>
 * Region resolution is typically hierarchical. When multiple regions contain the same
 * location, the most specific (deepest in the hierarchy) region should be returned.
 *
 * <p>
 * This is a domain-level contract and should not contain infrastructure concerns
 * such as persistence or framework-specific behavior.
 */
public interface RegionQueryService {
    /**
     * Registers a region in the current world context.
     *
     * <p>
     * Implementations may store the region in memory, index it spatially,
     * or prepare it for fast resolution during the game loop.
     *
     * @param region the region to register
     */
    void registerRegion(RegionNode region);

    /**
     * Retrieves a region by its unique identifier.
     *
     * @param name the region unique identifier
     * @return an {@link Optional} containing the region if found, otherwise empty
     */
    Optional<RegionNode> getRegion(String name);

    /**
     * Resolves the most specific region that contains the given location.
     *
     * <p>
     * If multiple regions match, the implementation should return the
     * deepest region in the hierarchy (e.g., BUILDING over CITY,
     * DUNGEON_LEVEL over DUNGEON).
     *
     * @param location the world location to evaluate
     * @return an {@link Optional} containing the resolved region if any,
     *         otherwise empty if no region contains the location
     */
    Optional<RegionNode> resolveRegion(Location location);
}
