package com.github.mayconr.juoserver.game.model;

import lombok.Builder;

@Builder
public record ItemOptions(ItemSpawnTarget target) {

    public sealed interface ItemSpawnTarget permits ContainerTarget, WorldLocationTarget, EquipTarget{
    }

    public record ContainerTarget(Container container) implements ItemSpawnTarget {
    }

    public record WorldLocationTarget(Location location) implements ItemSpawnTarget {
    }

    public record EquipTarget(UOMobile mobile) implements ItemSpawnTarget {
    }
}
