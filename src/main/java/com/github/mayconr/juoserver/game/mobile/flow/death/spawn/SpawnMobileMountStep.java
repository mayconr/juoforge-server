package com.github.mayconr.juoserver.game.mobile.flow.death.spawn;

import com.github.mayconr.juoserver.game.mobile.flow.death.DeathContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpawnMobileMountStep extends AbstractFlowStep<DeathContext> {
    public SpawnMobileMountStep() {
        super("spawn_mobile_mount_step");
    }

    @Override
    public StepResult execute(DeathContext context) {
        var victim = context.getVictim();
        if (victim.isMounted()) {
            // TODO recovery mount and spawn npc
            log.info("Unimplemented mount spawn");
        }

        return StepResult.success();
    }
}
