package com.github.mayconr.juoserver.game.spell;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.spell.template.SpellTemplate;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastContext;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastRegistry;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;

@Slf4j
public class SpellModuleImpl implements SpellModule {
    private final TemplateRegistry<String, SpellTemplate> spells;
    private final Map<Integer, SpellTemplate> spellsByClientId;
    private final SpellCastRegistry triggers;

    public SpellModuleImpl(TemplateRegistry<String, SpellTemplate> spells, SpellCastRegistry triggers) {
        this.triggers = Objects.requireNonNull(triggers, "Spell triggers are required");
        this.spells = Objects.requireNonNull(spells, "Spell templates are required");
        var keys = new HashSet<String>();
        var clientSpells = new HashMap<Integer, SpellTemplate>();
        for (var spell : spells.all()) {
            if (!keys.add(spell.key())) {
                throw new IllegalArgumentException("Duplicate spell key: " + spell.key());
            }
            if (spell.clientSpellId() != null && clientSpells.putIfAbsent(spell.clientSpellId(), spell) != null) {
                throw new IllegalArgumentException("Duplicate client spell ID: " + spell.clientSpellId());
            }
        }
        this.spellsByClientId = Map.copyOf(clientSpells);
    }

    @Override
    public Optional<SpellTemplate> getSpellByClientId(int clientSpellId) {
        return Optional.ofNullable(spellsByClientId.get(clientSpellId));
    }

    @Override
    public void castSpell(UOMobile caster, String spellKey) {
        Objects.requireNonNull(caster, "Spell caster is required");
        Objects.requireNonNull(spellKey, "Spell key is required");
        var matches = spells.get(spellKey);
        if (matches.isEmpty()) {
            log.warn("Spell cast request | Caster: {} (serial={}) | Unknown spell key: {}",
                    caster.getName(), caster.getSerialId(), spellKey);
            return;
        }
        var spell = matches.getFirst();
        log.info("Spell cast request | Caster: {} (serial={}) | Spell: {} (key={}) | Client spell ID: {} | Metadata: {}",
                caster.getName(), caster.getSerialId(), spell.name(), spell.key(), spell.clientSpellId(), spell.metadata());
        if (!triggers.dispatch(new SpellCastContext(caster, spell))) {
            log.warn("No spell cast trigger registered for spell {} ({})", spell.key(), spell.name());
        }
    }
}
