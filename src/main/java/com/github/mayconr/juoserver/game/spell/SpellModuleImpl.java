package com.github.mayconr.juoserver.game.spell;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.SpellbookType;
import com.github.mayconr.juoserver.game.spell.flow.open.OpenSpellBookContext;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.game.spell.template.SpellTemplate;
import com.github.mayconr.juoserver.game.spell.flow.cast.CastSpellContext;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;

public class SpellModuleImpl implements SpellModule {
    private final Map<Integer, SpellTemplate> spellsByClientId;
    private ModuleContext.FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Override
    public void openSpellBook(UOPlayer player, UOItem book, SpellbookType type, long spellMask) {
        flows.execute(new OpenSpellBookContext(player, book, type, spellMask));
    }

    public SpellModuleImpl(TemplateRegistry<String, SpellTemplate> spells) {
        Objects.requireNonNull(spells, "Spell templates are required");
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
        flows.execute(new CastSpellContext(caster, spellKey));
    }
}
