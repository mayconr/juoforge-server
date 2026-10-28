package com.github.mayconr.juoserver.game.ai.flow.activation;

import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public final class ActivateRegionContext extends AbstractSyncFlowContext<Void> {
    private final RegionNode region;
    private final List<UONpc> npcs;
}
