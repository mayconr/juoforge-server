package com.github.mayconr.juoserver.infrastructure.region;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.RegionType;

import java.util.List;
import java.util.Optional;

public interface RegionSystem {

    Optional<RegionNode> getRegion(String name);

    Optional<RegionNode> getRegion(Location location);

    /**
     * Finds the nearest region with {@code aiActivationScope} enabled, starting
     * with the supplied region itself.
     * <p>
     * If the supplied region is an AI activation scope, it is returned immediately.
     * Otherwise, the search follows its parent chain up to the root and returns
     * the first ancestor with {@code aiActivationScope} enabled.
     *
     * @param region the region or microregion from which to start the search;
     *               may be {@code null}
     * @return the supplied region or its nearest ancestor with AI activation
     *         scope enabled, or {@link Optional#empty()} if none is found or
     *         {@code region} is {@code null}
     */
    Optional<RegionNode> findAiActivationRegion(RegionNode region);

    List<RegionNode> getRegionsByType(RegionType type);
}

