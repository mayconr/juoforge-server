package com.github.mayconr.juoserver.game.skill.flow.update;

import com.github.mayconr.juoserver.game.skill.SkillAction;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class UpdateSkillActionsContext extends AbstractSyncFlowContext<Void> {
    private final double delta;
    private final List<Entry<Integer, SkillAction>> readyActions = new ArrayList<>();
}
