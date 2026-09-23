package com.github.mayconr.juoserver.game.spell.flow.cast.dispatch;

import com.github.mayconr.juoserver.game.spell.flow.cast.CastSpellContext;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastContext;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastRegistry;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DispatchSpellCastStep extends AbstractFlowStep<CastSpellContext> {
    private final SpellCastRegistry triggers;

    public DispatchSpellCastStep(SpellCastRegistry triggers) {
        super("DispatchSpellCast");
        this.triggers = triggers;
    }

    @Override
    public StepResult execute(CastSpellContext context) {
        var spell = context.getSpell();
        if (!triggers.dispatch(new SpellCastContext(context.getCaster(), spell))) {
            log.warn("No spell cast trigger registered for spell {} ({})", spell.key(), spell.name());
            return StepResult.stop("UNHANDLED_SPELL", "No spell cast trigger registered for " + spell.key());
        }
        return StepResult.success();
    }
}
