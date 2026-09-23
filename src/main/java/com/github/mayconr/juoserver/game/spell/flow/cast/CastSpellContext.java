package com.github.mayconr.juoserver.game.spell.flow.cast;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.spell.template.SpellTemplate;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class CastSpellContext extends AbstractSyncFlowContext<Void> {
    private final UOMobile caster;
    private final String spellKey;
    @Setter
    private SpellTemplate spell;
}
