package com.github.mayconr.juoserver.shard.commands;

import com.github.mayconr.juoserver.common.event.HandlerResult;
import com.github.mayconr.juoserver.common.event.Prompt;
import com.github.mayconr.juoserver.game.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.gump.GumpSystem;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.session.world.WorldSession;

import static com.github.mayconr.juoserver.game.gump.DeclarativeGumpUI.*;

public class Goto extends AbstractCommand {

    private final GumpSystem gumpSystem;
    private final WorldSession worldSession;

    public Goto(GumpSystem gumpSystem, WorldSession worldSession) {
        super("goto");
        this.gumpSystem = gumpSystem;
        this.worldSession = worldSession;
    }

    @Override
    public HandlerResult handle(Prompt event) {
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
                event.mobile(),
                gump,
                (c, e) -> {
                    switch (e.getButtonId()) {
                        case 100 -> worldSession
                                .getPlayerSession(c.player())
                                .move(new PointInTheWorld(1466, 1715, 0));
                        case 101 -> worldSession
                                .getPlayerSession(c.player())
                                .move(new PointInTheWorld(2516, 531, 0));
                    }
                });
        return HandlerResult.CONTINUE;
    }
}
