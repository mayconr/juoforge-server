package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.world.World;

import static com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI.*;

public class Goto extends AbstractCommand {

    private final World world;

    public Goto(ServerRuntime runtime) {
        super("goto");
        this.world = runtime.world();
    }

    @Override
    public void handle(Prompt event) {
        if (event.arguments().length == 0) {
            showGump(event);
        } else {
            int x = Integer.parseInt(event.arguments()[0]);
            int y = Integer.parseInt(event.arguments()[1]);
            int z = Integer.parseInt(event.arguments()[2]);
            world.teleport(event.player(), new PointInTheWorld(x, y, z));
        }
    }

    private void showGump(Prompt event) {
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
                                                Button(1209, 1210, 102, "Moonglow"),
                                                Button(1209, 1210, 103, "Mine")
                                        ))));
        world.sendGump(
                event.player(),
                gump,
                (c, e) -> {
                    switch (e.getButtonId()) {
                        case 100 -> world.teleport(c.player(), new PointInTheWorld(1466, 1715, 0));
                        case 101 -> world.teleport(c.player(), new PointInTheWorld(2516, 531, 0));
                        case 102 -> world.teleport(c.player(), new PointInTheWorld(4427, 1140, 0));
                        case 103 -> world.teleport(c.player(), new PointInTheWorld(5753, 324, 27));
                    }
                });
    }
}
