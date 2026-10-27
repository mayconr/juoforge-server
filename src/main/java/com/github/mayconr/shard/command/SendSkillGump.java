package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.model.SendSkillType;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

public final class SendSkillGump extends AbstractCommand {
    private final World world;

    public SendSkillGump(ServerRuntime runtime) {
        super("sendskillgump");
        this.world = runtime.world();
    }

    @Override
    public void handle(Prompt event) {
        world.ui().sendSkillGump(event.player(), event.player().getSerialId(), SendSkillType.FULL_LIST_WITH_CAP_AND_OPEN);
    }
}
