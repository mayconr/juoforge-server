package com.github.mayconr.juoserver.game.skill.flow.perform;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.skill.SkillPerformOptions;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public final class PerformSkillContext extends AbstractSyncFlowContext<Boolean> {
    private final UOPlayer player;
    private final SkillPerformOptions options;
    @Setter
    private int swings = 1;
}
