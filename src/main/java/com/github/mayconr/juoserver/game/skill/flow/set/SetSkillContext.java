package com.github.mayconr.juoserver.game.skill.flow.set;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class SetSkillContext extends AbstractSyncFlowContext<Void> {
    private final UOPlayer player;
    private final int skillId;
    private final double value;
}
