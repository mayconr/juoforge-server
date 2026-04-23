package com.github.mayconr.juoserver.game.npc.flow.creation;

import com.github.mayconr.juoserver.game.flow.NpcCreationFlowDefinition.NpcCreationContext;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class NpcSerialGenStep extends AbstractFlowStep<NpcCreationContext> {

    private final SerialGenerator serialGenerator;

    public NpcSerialGenStep(int order, SerialGenerator serialGenerator) {
        super("GenerateSerialId", order, FlowPhase.CORE);
        this.serialGenerator = serialGenerator;
    }

    @Override
    public StepResult execute(NpcCreationContext context) {
        context.setSerialId(serialGenerator.getNextMobile());
        return StepResult.success();
    }
}
