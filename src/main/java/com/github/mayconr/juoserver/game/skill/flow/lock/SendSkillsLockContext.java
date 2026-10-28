package com.github.mayconr.juoserver.game.skill.flow.lock;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import com.github.mayconr.juoserver.game.model.SkillValue;
import java.util.Collection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class SendSkillsLockContext extends AbstractSyncFlowContext<Void> {
    private final UOPlayer player;
    private final Collection<SkillValue> skills;
}
