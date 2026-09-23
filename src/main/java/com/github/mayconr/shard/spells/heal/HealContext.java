package com.github.mayconr.shard.spells.heal;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class HealContext extends AbstractSyncFlowContext<Void> {
    private final UOMobile caster;
}
