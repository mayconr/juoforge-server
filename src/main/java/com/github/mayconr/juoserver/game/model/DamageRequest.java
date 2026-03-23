package com.github.mayconr.juoserver.game.model;

import java.util.List;

public record DamageRequest(UOMobile source, UOMobile target, DamageSourceKind sourceKind, List<DamageComponent> components) {

}
