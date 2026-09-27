package com.github.mayconr.juoserver.game.spell.trigger;

/** Shard-provided behavior for a spell cast request. Instances may receive concurrent requests. */
public interface SpellCastTrigger {
    boolean supports(SpellCastContext context);

    /**
     * Handles or starts the request; returning does not imply casting has completed.
     * Do not call castSpell again for the same request, as that would dispatch recursively.
     */
    void execute(SpellCastContext context);
}
