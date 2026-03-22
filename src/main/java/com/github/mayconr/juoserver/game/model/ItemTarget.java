package com.github.mayconr.juoserver.game.model;

public sealed interface ItemTarget permits ContainerItemTarget, GroundItemTarget, EquipItemTarget {
}
