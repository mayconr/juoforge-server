package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.world.WorldActions;
import com.github.mayconr.juoserver.game.session.world.WorldView;
import com.github.mayconr.shard.skills.Skills;

public class Test extends AbstractCommand {

    private final WorldActions worldActions;
    private final WorldView worldView;

    public Test(WorldActions worldActions, WorldView worldView) {
        super("test");
        this.worldActions = worldActions;
        this.worldView = worldView;
    }

    @Override
    public void handle(Prompt event) {
        worldActions.sendTarget((UOPlayer) event.mobile(), CursorType.NEUTRAL, result->{
            final var item = worldView.getContainerBySerialId(result.serialId()).orElseThrow(()->new IllegalStateException("item not found"));
            //event.mobile().getSkills().get(Skills.MINING.getId()).setBase(Double.parseDouble(event.arguments()[0]));
            //worldActions.createItemInContainer("iron_ore", item);
        });

    }
}
