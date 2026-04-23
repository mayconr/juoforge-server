package com.github.mayconr.juoserver.game.npc.flow.creation;

import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.flow.NpcCreationFlowDefinition.NpcCreationContext;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class AttachAIStep extends AbstractFlowStep<NpcCreationContext> {

    private final AIModule aiModule;
    private final World world;

    public AttachAIStep(int order, AIModule aiModule, World world) {
        super("AttachAI", order, FlowPhase.CORE);
        this.aiModule = aiModule;
        this.world = world;
    }

    @Override
    public StepResult execute(NpcCreationContext context) {
        var ai = aiModule.attach(context.getNpc());
        if (ai != null) {
            ai.wakeup(world);
        }
        return StepResult.success();
    }
}
