package com.github.mayconr.juoserver.game.skill.flow.swings;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class SetSkillSwingCountContext extends AbstractSyncFlowContext<Void> {
    private final UOPlayer player;
    private final int swingCount;
}
