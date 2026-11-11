package com.github.mayconr.juoserver.game.npc.flow.removal.removal;

import com.github.mayconr.juoserver.game.npc.flow.removal.NpcRemovalContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public class RemoveNpcStep extends AbstractFlowStep<NpcRemovalContext> {

    private final WorldStorage storage;

    public RemoveNpcStep(WorldStorage storage) {
        super("RemoveNpc");
        this.storage = storage;
    }

    @Override
    public StepResult execute(NpcRemovalContext context) {
        storage.deleteMobile(context.getNpc());
        return StepResult.success();
    }
}
