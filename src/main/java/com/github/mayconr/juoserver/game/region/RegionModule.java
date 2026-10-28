package com.github.mayconr.juoserver.game.region;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;

import java.util.Optional;

public interface RegionModule extends WorldRegion, WorldModule {
    /** Updates region membership and AI; null positions represent entering or leaving the world. */
    void regionTransition(UOMobile mobile, Location from, Location to);

    /** Internal spawn operation; registers the player in its current region and ancestors. */
    Optional<RegionNode> registerPlayer(UOPlayer player);

    /** Internal flow operation; null removes the player from the index. */
    void updatePlayerRegion(UOPlayer player, RegionNode region);
}
