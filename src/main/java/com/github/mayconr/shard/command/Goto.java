package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.model.event.message.LocalizedMessageContent;
import com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.world.WorldActions;

import static com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI.*;

public class Goto extends AbstractCommand {

    private final WorldActions worldActions;

    public Goto(WorldActions worldActions) {
        super("goto");
        this.worldActions = worldActions;
    }

    @Override
    public void handle(Prompt event) {
        worldActions.sendMessage(event.player(), new LocalizedMessageContent("test"));

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
                                                Button(1209, 1210, 101, "Minoc"),
                                                Button(1209, 1210, 102, "Moonglow")
                                                ))));
        worldActions.sendGump(
                event.player(),
                gump,
                (c, e) -> {
                    switch (e.getButtonId()) {
                        case 100 -> worldActions.teleport(c.player(), new PointInTheWorld(1466, 1715, 0));
                        case 101 -> worldActions.teleport(c.player(), new PointInTheWorld(2516, 531, 0));
                        case 102 -> worldActions.teleport(c.player(), new PointInTheWorld(4427, 1140, 0));
                    }
                });
    }
}
