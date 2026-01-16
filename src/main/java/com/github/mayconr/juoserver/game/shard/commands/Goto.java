package com.github.mayconr.juoserver.game.shard.commands;

import com.github.mayconr.juoserver.game.core.event.HandlerResult;
import com.github.mayconr.juoserver.game.core.event.Prompt;
import com.github.mayconr.juoserver.game.core.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.core.gump.GumpSystem;
import com.github.mayconr.juoserver.game.core.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.core.session.game.GameSession;

import static com.github.mayconr.juoserver.game.core.gump.DeclarativeGumpUI.*;

public class Goto extends AbstractCommand {

    private final GumpSystem gumpSystem;
    private final GameSession gameSession;

    public Goto(GumpSystem gumpSystem, GameSession gameSession) {
        super("goto");
        this.gumpSystem = gumpSystem;
        this.gameSession = gameSession;
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
                        case 100 -> gameSession
                                .getPlayerSession(c.player())
                                .move(new PointInTheWorld(1466, 1715, 0));
                        case 101 -> gameSession
                                .getPlayerSession(c.player())
                                .move(new PointInTheWorld(2516, 531, 0));
                    }
                });
        return HandlerResult.CONTINUE;
    }
}
