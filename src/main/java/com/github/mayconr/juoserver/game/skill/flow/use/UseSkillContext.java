package com.github.mayconr.juoserver.game.skill.flow.use;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class UseSkillContext extends AbstractSyncFlowContext<Void> {

    private final UOPlayer player;
    private final int skillId;

}
