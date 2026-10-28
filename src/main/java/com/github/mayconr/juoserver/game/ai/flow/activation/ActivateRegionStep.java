package com.github.mayconr.juoserver.game.ai.flow.activation;

import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ActivateRegionStep extends AbstractFlowStep<ActivateRegionContext> {
    private final AIModule ai;

    public ActivateRegionStep(AIModule ai) {
        super("ActivateRegion");
        this.ai = ai;
    }

    @Override
    public StepResult execute(ActivateRegionContext context) {
        var region = context.getRegion();
        int activated = 0;
        for (var npc : context.getNpcs()) {
            if (npc.isAlive() && region.contains(npc)) {
                ai.activate(npc);
                activated++;
            }
        }
        log.debug("AI activation completed in region [{}]: {} NPCs", region.getName(), activated);
        return StepResult.success();
    }
}
