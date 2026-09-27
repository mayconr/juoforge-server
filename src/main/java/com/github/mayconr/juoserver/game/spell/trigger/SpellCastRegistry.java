package com.github.mayconr.juoserver.game.spell.trigger;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class SpellCastRegistry {
    private final CopyOnWriteArrayList<SpellCastTrigger> triggers = new CopyOnWriteArrayList<>();

    /** Registers a trigger during bootstrap. Earlier registrations take precedence. */
    public void register(SpellCastTrigger trigger) {
        triggers.add(Objects.requireNonNull(trigger, "Spell trigger factory must not return null"));
    }

    /** Executes only the first matching trigger; returns false when no trigger supports the request. */
    public boolean dispatch(SpellCastContext context) {
        Objects.requireNonNull(context, "Spell context is required");
        for (var trigger : triggers) {
            if (trigger.supports(context)) {
                trigger.execute(context);
                return true;
            }
        }
        return false;
    }
}
