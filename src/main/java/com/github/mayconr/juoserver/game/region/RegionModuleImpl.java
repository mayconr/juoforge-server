package com.github.mayconr.juoserver.game.region;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.region.flow.transition.RegionTransitionContext;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.infrastructure.region.RegionSystem;
import java.util.*;

public final class RegionModuleImpl implements RegionModule {
    private final RegionSystem regionSystem;
    private final Map<RegionNode, Set<Integer>> playersByRegion = new HashMap<>();
    private final Map<Integer, Set<RegionNode>> regionsByPlayer = new HashMap<>();
    private ModuleContext.FlowFacade flows;

    public RegionModuleImpl(RegionSystem regionSystem) {
        this.regionSystem = Objects.requireNonNull(regionSystem, "regionSystem");
    }

    @Override
    public Optional<RegionNode> getRegion(UOMobile mobile) {
        return regionSystem.getRegion(Objects.requireNonNull(mobile, "mobile"));
    }

    @Override
    public void initialize(ModuleContext context) {
        flows = context.flows();
    }

    @Override
    public void regionTransition(UOMobile mobile, Location from, Location to) {
        var result = flows.execute(new RegionTransitionContext(mobile, from, to));
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("regionTransition", result, null);
        }
    }

    @Override
    public synchronized Set<Integer> getPlayerSerials(RegionNode region) {
        Objects.requireNonNull(region, "region");
        return Set.copyOf(playersByRegion.getOrDefault(region, Set.of()));
    }

    @Override
    public Optional<RegionNode> registerPlayer(UOPlayer player) {
        var region = getRegion(player);
        updatePlayerRegion(player, region.orElse(null));
        return region;
    }

    @Override
    public synchronized void updatePlayerRegion(UOPlayer player, RegionNode region) {
        int serial = player.getSerialId();
        var current = new HashSet<RegionNode>();
        for (var node = region; node != null; node = node.getParent().orElse(null)) {
            current.add(node);
        }
        var previous = regionsByPlayer.remove(serial);
        if (previous != null) {
            for (var node : previous) {
                if (current.contains(node)) continue;
                var players = playersByRegion.get(node);
                players.remove(serial);
                if (players.isEmpty()) playersByRegion.remove(node);
            }
        }
        for (var node : current) {
            playersByRegion.computeIfAbsent(node, key -> new HashSet<>()).add(serial);
        }
        if (!current.isEmpty()) regionsByPlayer.put(serial, current);
    }
}
