package com.github.mayconr.juoserver.game.skill.flow.stop;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Result is true when the player had an active action whose stop was requested. */
@Getter
@RequiredArgsConstructor
public final class StopSkillActionContext extends AbstractSyncFlowContext<Boolean> {
    private final UOPlayer player;
}
