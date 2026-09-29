package com.github.mayconr.juoserver.game.mobile.flow.mount.conversion;

import com.github.mayconr.juoserver.game.mobile.flow.mount.MountContext;
import com.github.mayconr.juoserver.game.npc.NpcModule;
import com.github.mayconr.juoserver.game.npc.NpcRequester;
import com.github.mayconr.juoserver.game.model.UOPlayer;
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

        NpcRequester requester = context.getMobile() instanceof UOPlayer player
                ? new NpcRequester.Player(player)
                : new NpcRequester.AsyncProcess("mount");
        npcModule.removeNpc(requester, mountNpc);

        return StepResult.success();
    }
}
