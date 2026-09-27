package com.github.mayconr.juoserver.game.spell;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.SpellbookType;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.game.spell.template.SpellTemplate;
import java.util.Optional;

public interface SpellModule extends WorldModule {
    void openSpellBook(UOPlayer player, UOItem book, SpellbookType type, long spellMask);

    /**
     * Receives a cast request using the shard's namespaced spell key.
     * Resolves and logs the spell, then dispatches to the first supporting shard trigger.
     */
    void castSpell(UOMobile caster, String spellKey);

    /** Resolves only spells explicitly mapped to a client ID. */
    Optional<SpellTemplate> getSpellByClientId(int clientSpellId);
}
