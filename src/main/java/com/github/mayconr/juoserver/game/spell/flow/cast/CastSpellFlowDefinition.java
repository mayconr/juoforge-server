package com.github.mayconr.juoserver.game.spell.flow.cast;

import com.github.mayconr.juoserver.game.spell.flow.cast.validation.ValidateCastSpellStep;
import com.github.mayconr.juoserver.game.spell.flow.cast.resolve.ResolveSpellStep;
import com.github.mayconr.juoserver.game.spell.flow.cast.dispatch.DispatchSpellCastStep;
import com.github.mayconr.juoserver.game.spell.template.SpellTemplate;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastRegistry;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class CastSpellFlowDefinition {
    private CastSpellFlowDefinition() {}

    public static Flow<CastSpellContext> build(TemplateRegistry<String, SpellTemplate> spells,
                                               SpellCastRegistry triggers) {
        return FlowFactory.<CastSpellContext>builder()
                .step(new ValidateCastSpellStep())
                .step(new ResolveSpellStep(spells))
                .step(new DispatchSpellCastStep(triggers))
                .build();
    }
}
