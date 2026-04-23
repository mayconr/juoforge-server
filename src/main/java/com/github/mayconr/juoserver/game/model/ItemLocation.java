package com.github.mayconr.juoserver.game.model;

public sealed interface ItemLocation permits ContainerLocation, EquippedLocation, GroundLocation, OrphanLocation {

}
