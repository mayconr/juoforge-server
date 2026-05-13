package com.github.mayconr.juoserver.game.model;

import com.github.mayconr.juoserver.game.model.ContainerItemTarget.Options;

import java.util.function.Consumer;

public sealed interface ItemTarget permits ContainerItemTarget, EquipItemTarget, GroundItemTarget, OrphanItemTarget {

    static EquipItemTarget equip(UOMobile mobile) {
        return new EquipItemTarget(mobile);
    }

    static ContainerItemTarget container(UOContainer container) {
        return ContainerItemTarget.of(container);
    }

    static ContainerItemTarget dropAt(UOContainer container, Consumer<Options.Builder> cfg) {
        return ContainerItemTarget.of(container, cfg);
    }

    static GroundItemTarget dropAt(Location location) {
        return new GroundItemTarget(location);
    }

    static OrphanItemTarget orphan() {
        return new OrphanItemTarget();
    }
}
