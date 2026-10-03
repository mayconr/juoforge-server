package com.github.mayconr.juoserver.game.npc.flow.creation;

import com.github.mayconr.juoserver.game.npc.NpcRequester;
import com.github.mayconr.juoserver.game.mobile.template.NpcTemplate;
import com.github.mayconr.juoserver.game.npc.stats.NpcStats;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.BehaviorDefinition;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class NpcCreationContext extends AbstractSyncFlowContext<Void> {
    @lombok.NonNull
    private final NpcRequester requester;
    private final String templateName;
    private final Location location;

    private UONpc npc;
    private Integer serialId;
    private NpcTemplate template;
    private NpcStats stats;
    private BehaviorDefinition behavior;
    private Map<Layer, Integer> equippedItems;
}
