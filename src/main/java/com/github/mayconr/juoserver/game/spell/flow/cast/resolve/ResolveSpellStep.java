package com.github.mayconr.juoserver.game.spell.flow.cast.resolve;

import com.github.mayconr.juoserver.game.spell.flow.cast.CastSpellContext;
import com.github.mayconr.juoserver.game.spell.template.SpellTemplate;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResolveSpellStep extends AbstractFlowStep<CastSpellContext> {
    private final TemplateRegistry<String, SpellTemplate> spells;

    public ResolveSpellStep(TemplateRegistry<String, SpellTemplate> spells) {
        super("ResolveSpell");
        this.spells = spells;
    }

    @Override
    public StepResult execute(CastSpellContext context) {
        var caster = context.getCaster();
        var matches = spells.get(context.getSpellKey());
        if (matches.isEmpty()) {
            log.warn("Spell cast request | Caster: {} (serial={}) | Unknown spell key: {}",
                    caster.getName(), caster.getSerialId(), context.getSpellKey());
            return StepResult.failure("UNKNOWN_SPELL", "Unknown spell key: " + context.getSpellKey());
        }
        var spell = matches.getFirst();
        context.setSpell(spell);
        log.info("Spell cast request | Caster: {} (serial={}) | Spell: {} (key={}) | Client spell ID: {} | Metadata: {}",
                caster.getName(), caster.getSerialId(), spell.name(), spell.key(), spell.clientSpellId(), spell.metadata());
        return StepResult.success();
    }
}
