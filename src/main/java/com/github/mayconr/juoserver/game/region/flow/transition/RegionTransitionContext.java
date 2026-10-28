package com.github.mayconr.juoserver.game.region.flow.transition;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;

@Getter
@RequiredArgsConstructor
public final class RegionTransitionContext extends AbstractSyncFlowContext<Void> {
    private final UOMobile mobile;
    private final Location from;
    private final Location to;
    @Setter
    private RegionNode oldRegion;
    @Setter
    private RegionNode newRegion;
}
