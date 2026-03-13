package com.github.mayconr.juoserver.infrastructure.region;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.RegionType;

import java.util.List;
import java.util.Optional;

public interface RegionSystem {

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
    Optional<RegionNode> getRegion(Location location);

    List<RegionNode> getRegionsByType(RegionType type);
}

