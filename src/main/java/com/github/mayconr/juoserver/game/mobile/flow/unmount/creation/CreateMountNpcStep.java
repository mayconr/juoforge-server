package com.github.mayconr.juoserver.game.mobile.flow.unmount.creation;

import com.github.mayconr.juoserver.game.mobile.flow.unmount.UnmountContext;
import com.github.mayconr.juoserver.game.npc.NpcModule;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateMountNpcStep extends AbstractFlowStep<UnmountContext> {

    private final NpcModule npcModule;

    public CreateMountNpcStep(NpcModule npcModule) {
        super("CreateMountNpc");
        this.npcModule = npcModule;
    }

    @Override
    public StepResult execute(UnmountContext context) {
        final var mountName = context.getMountNpcName();
        final var mobile = context.getMobile();

        var npc = npcModule.createNpc(mountName, mobile);
        if (log.isDebugEnabled()) {
            log.debug("Created NPC {}", npc.getName());
        }

        return StepResult.success();
    }
}
