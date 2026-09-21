package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record CombatOccurring(
        UOMobile attacker,
        UOMobile target,
        int hitFrame,
        OccurringType type
) implements GameEvent {

    public sealed interface OccurringType {
    }

    public record MeleeType(UOItem weaponItem) implements OccurringType { }

    public record WrestlingType() implements OccurringType { }

    public record RangedType(UOItem weaponItem) implements OccurringType {}

    public record SpellType(UOItem weaponItem) implements OccurringType {}
}
