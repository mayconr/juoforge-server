package com.github.mayconr.juoserver.game.npc.flow.removal;

import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.npc.NpcRequester;
import lombok.NonNull;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class NpcRemovalContext extends AbstractSyncFlowContext<Void> {
    @NonNull
    private final NpcRequester requester;
    private final UONpc npc;
}
