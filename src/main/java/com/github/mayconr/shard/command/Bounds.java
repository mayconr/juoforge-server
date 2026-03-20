package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class Bounds extends AbstractCommand{

    public static final String LOCATIONS = ".BOUNDS_LOCATIONS";
    private static final String ITEMS = ".BOUNDS_ITEMS";
    private final World world;

    public Bounds(ServerRuntime runtime) {
        super("bounds");
        this.world = runtime.world();
    }

    @Override
    public void handle(Prompt event) {
        final var player = event.player();
        final var subCmd = event.arguments()[0];

        switch (subCmd){
            case "add" -> add(player);
            case "set" -> set(player, event.arguments()[1]);
            case "draw" -> draw(player);
            case "refresh" -> refresh(player);
            case "clear" -> clear(player);
            case "info" -> info(player);
            default -> world.sendMessage(player, "Invalid sub-command");
        }
    }

    private void info(UOPlayer player) {
        var attributes = player.runtimeAttributes();

        final StringBuilder info = new StringBuilder();
        for (Location location : attributes.getOrDefault(LOCATIONS, Collections.<Location>emptyList())) {
            info.append("x:").append(location.getX())
                    .append(",y:").append(location.getY())
                    .append(",z:").append(location.getZ())
                    .append(" ");
        }
        log.info(info.toString());
        world.sendMessage(player, info.toString());
    }

    private void set(UOPlayer player, String index) {
        world.sendMessage(player, "Select a region");
        world.sendTarget(player, CursorType.NEUTRAL, result->{
            var attributes = player.runtimeAttributes();

            attributes.getOrDefault(LOCATIONS, Collections.<Location>emptyList()).set(Integer.parseInt(index) - 1, result.location());

            refresh(player);

            world.sendMessage(player, "Location updated!");
        });
    }

    private void add(UOPlayer player) {
        var attributes = player.runtimeAttributes();
        world.sendMessage(player, "Select a region");
        world.sendTarget(player, CursorType.NEUTRAL, result->{
            attributes.computeIfAbsent(LOCATIONS, ArrayList::new)
                    .add(result.location());

            refresh(player);

            world.sendMessage(player, "Location added!");
        });
    }

    private void refresh(UOPlayer player) {
        var attributes = player.runtimeAttributes();

        for (Integer serial : attributes.getOrDefault(ITEMS, Collections.<Integer>emptyList())) {
            world.deleteItem(serial);
        }

        draw(player);
    }

    private void clear(UOPlayer player) {
        var attributes = player.runtimeAttributes();

        for (Integer serial : attributes.getOrDefault(ITEMS, Collections.<Integer>emptyList())) {
            world.deleteItem(serial);
        }
        attributes.remove(LOCATIONS);
        attributes.remove(ITEMS);

        world.sendMessage(player, "Bounds cleared!");
    }

    private void draw(UOPlayer player) {
        var attributes = player.runtimeAttributes();

        final List<Location> locations = attributes.getOrDefault(LOCATIONS, Collections.emptyList());

        if(locations.size() < 2){
            return;
        }

        List<Integer> boundItems = new ArrayList<>();

        for(int i = 0; i < locations.size(); i++){

            Location from = locations.get(i);
            Location to = locations.get((i + 1) % locations.size());

            List<Location> line = GameMath.line(from, to);

            for (Location location : line) {
                createItem(boundItems, "marker", location);

                if (from.equals(location)) {
                    createItem(boundItems, "marker_flag", location);
                }
            }
        }

        attributes.set(ITEMS, boundItems);
    }

    private void createItem(List<Integer> boundItems, String name, Location location) {
        int serialFlag = world.createItem(ItemRequest.byName(name).build(), GroundItemTarget.of(location))
                .getSerialId();
        boundItems.add(serialFlag);
    }
}
