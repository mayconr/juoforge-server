package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@Slf4j
public class Debug extends AbstractCommand {

    private static final int DEBUG_RADIUS = 24;

    private final World world;

    public Debug(World world) {
        super("debug");
        this.world = world;
    }

    @Override
    public void handle(Prompt event) {
        var args = event.arguments();
        if (args.length != 1) {
            logUsage();
            return;
        }
        if ("onlinePlayers".equalsIgnoreCase(args[0])) {
            logOnlinePlayers();
        } else if ("mobilesInRange".equalsIgnoreCase(args[0])) {
            logMobilesInRange(event.player());
        } else {
            logUsage();
        }
        world.message().send(event.player(), "All the information were printed in the server console!");
    }

    private void logUsage() {
        log.info("""

                ========== DEBUG HELP ==========
                  debug onlinePlayers
                    Lists only online players, regardless of location.
                  debug mobilesInRange
                    Lists all nearby players and NPCs, alive or dead, within {} tiles.
                ================================
                """, DEBUG_RADIUS);
    }

    private void logOnlinePlayers() {
        var onlinePlayers = world.player().getOnlinePlayers();
        var details = onlinePlayers.stream()
                .map(player -> "  - Serial: %d | Name: %s | Position: (%d, %d, %d) | Connected: %s | State: %s".formatted(
                        player.getSerialId(), player.getName(), player.getX(), player.getY(), player.getZ(),
                        player.isConnected() ? "Yes" : "No", player.isAlive() ? "Alive" : "Dead"))
                .collect(Collectors.joining("\n"));
        log.info("""

                ========== ONLINE PLAYERS ==========
                Only online players, regardless of location.
                Total: {}
                {}
                ====================================
                """, onlinePlayers.size(), onlinePlayers.isEmpty() ? "  No online players found." : details);
    }

    private void logMobilesInRange(UOPlayer source) {
        var mobiles = world.storage().getMobilesInRange(source, DEBUG_RADIUS, mobile -> true);
        var details = mobiles.stream()
                .map(mobile -> "  - Class: %s | Serial: %d | Name: %s | Position: (%d, %d, %d) | State: %s".formatted(
                        mobile.getClass().getSimpleName(), mobile.getSerialId(), mobile.getName(),
                        mobile.getX(), mobile.getY(), mobile.getZ(), mobile.isAlive() ? "Alive" : "Dead"))
                .collect(Collectors.joining("\n"));
        log.info("""

                ========== NEARBY MOBILES ==========
                All nearby players and NPCs, alive or dead.
                Center: ({}, {}, {}) | Radius: {} tiles | Total: {}
                {}
                ====================================
                """, source.getX(), source.getY(), source.getZ(), DEBUG_RADIUS, mobiles.size(),
                mobiles.isEmpty() ? "  No nearby mobiles found." : details);
    }
}
