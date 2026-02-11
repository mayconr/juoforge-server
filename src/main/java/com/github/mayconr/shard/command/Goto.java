package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.gump.GumpSystem;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.world.WorldActions;

import static com.github.mayconr.juoserver.game.gump.DeclarativeGumpUI.*;

public class Goto extends AbstractCommand {

    private final GumpSystem gumpSystem;
    private final WorldActions worldActions;

    public Goto(GumpSystem gumpSystem, WorldActions worldActions) {
        super("goto");
        this.gumpSystem = gumpSystem;
        this.worldActions = worldActions;
    }

    @Override
    public void handle(Prompt event) {
        DeclarativeGumpUI gump =
                new DeclarativeGumpUI(
                        Page(
                                1,
                                Panel(
                                        5594,
                                        383,
                                        383,
                                        false,
                                        Row(
                                                2,
                                                Button(1209, 1210, 100, "Britain"),
                                                Button(1209, 1210, 101, "Minoc")))));
        gumpSystem.send(
                event.player(),
                gump,
                (c, e) -> {
                    switch (e.getButtonId()) {
                        case 100 -> worldActions.teleport(c.player(), new PointInTheWorld(1466, 1715, 0));
                        case 101 -> worldActions.teleport(c.player(), new PointInTheWorld(2516, 531, 0));
                    }
                });
    }
}
