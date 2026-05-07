package com.github.mayconr.juoserver.game.npc.flow.creation.identity;

import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationContext;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class NpcSerialGenStep extends AbstractFlowStep<NpcCreationContext> {

    private final SerialGenerator serialGenerator;

    public NpcSerialGenStep(SerialGenerator serialGenerator) {
        super("GenerateSerialId");
        this.serialGenerator = serialGenerator;
    }

    @Override
    public StepResult execute(NpcCreationContext context) {
        context.setSerialId(serialGenerator.getNextMobile());
        return StepResult.success();
    }
}
