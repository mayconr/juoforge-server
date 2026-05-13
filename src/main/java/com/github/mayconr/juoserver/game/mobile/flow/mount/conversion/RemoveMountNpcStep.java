package com.github.mayconr.juoserver.game.mobile.flow.mount.conversion;

import com.github.mayconr.juoserver.game.mobile.flow.mount.MountContext;
import com.github.mayconr.juoserver.game.npc.NpcModule;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class RemoveMountNpcStep extends AbstractFlowStep<MountContext> {

    private final NpcModule npcModule;

    public RemoveMountNpcStep(NpcModule npcModule) {
        super("RemoveMountNpc");
        this.npcModule = npcModule;
    }

    @Override
    public StepResult execute(MountContext context) {
        final var mountNpc = context.getMountNpc();

        npcModule.removeNpc(mountNpc);

        return StepResult.success();
    }
}
