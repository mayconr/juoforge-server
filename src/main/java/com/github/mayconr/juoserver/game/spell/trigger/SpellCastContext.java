package com.github.mayconr.juoserver.game.spell.trigger;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.spell.template.SpellTemplate;

import java.util.Objects;

/** A resolved cast request, independent of packets, gumps, or NPC actions. */
public record SpellCastContext(UOMobile caster, SpellTemplate spell) {
    public SpellCastContext {
        Objects.requireNonNull(caster, "Spell caster is required");
        Objects.requireNonNull(spell, "Spell template is required");
    }
}
