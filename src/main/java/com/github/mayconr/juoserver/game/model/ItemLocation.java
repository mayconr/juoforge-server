package com.github.mayconr.juoserver.game.model;

public sealed interface ItemLocation permits ContainerLocation, EquippedLocation, GroundLocation, OrphanLocation {

    static ContainerLocation container(int containerSerialId) {
        return new ContainerLocation(containerSerialId);
    }

    static EquippedLocation equipped(int ownerSerialId) {
        return new EquippedLocation(ownerSerialId);
    }

    static GroundLocation ground() {
        return new GroundLocation();
    }

    static OrphanLocation orphan() {
        return new OrphanLocation();
    }
}
