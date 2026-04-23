package com.github.mayconr.juoserver.game.npc.flow.removal;

import com.github.mayconr.juoserver.game.flow.NpcRemovalFlowDefinition;
import com.github.mayconr.juoserver.game.flow.NpcRemovalFlowDefinition.NpcRemovalContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class RemoveNpcStep extends AbstractFlowStep<NpcRemovalContext> {

    private final RealmStorage storage;

    public RemoveNpcStep(int order, RealmStorage storage) {
        super("RemoveNpc", order, FlowPhase.CORE);
        this.storage = storage;
    }

    @Override
    public StepResult execute(NpcRemovalContext context) {
        storage.deleteMobile(context.getNpc());
        return StepResult.success();
    }
}
