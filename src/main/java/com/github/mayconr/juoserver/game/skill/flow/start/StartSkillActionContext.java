package com.github.mayconr.juoserver.game.skill.flow.start;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.skill.SkillActionOptions;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public final class StartSkillActionContext extends AbstractSyncFlowContext<Boolean> {
    private final UOPlayer player;
    private final SkillActionOptions options;
    @Setter
    private int swings = 1;
    /** Player position when the action was requested; moving away from it cancels the remaining swings. */
    @Setter
    private Location origin;
}
