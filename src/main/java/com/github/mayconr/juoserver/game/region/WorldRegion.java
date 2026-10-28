package com.github.mayconr.juoserver.game.region;

import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.game.model.UOMobile;
import java.util.Optional;
import java.util.Set;

public interface WorldRegion {
    /** Resolves the most specific region containing the mobile's current position. */
    Optional<RegionNode> getRegion(UOMobile mobile);

    /** Immutable snapshot including descendants, updated on player spawn and movement. */
    Set<Integer> getPlayerSerials(RegionNode region);
}
